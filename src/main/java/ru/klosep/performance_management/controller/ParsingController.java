package ru.klosep.performance_management.controller;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import ru.klosep.performance_management.model.ParseJob;
import ru.klosep.performance_management.service.TaskParsingService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api/parse")
public class ParsingController {

    private final ConcurrentHashMap<String, ParseJob> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TaskParsingService parsingService;

    @Autowired
    private MeterRegistry meterRegistry;

    private final Counter pollingRequestsCounter;
    private final Counter wsRequestsCounter;

    public ParsingController(MeterRegistry registry) {
        this.pollingRequestsCounter = Counter.builder("parsing.requests.polling")
                .description("Number of polling parsing requests")
                .register(registry);

        this.wsRequestsCounter = Counter.builder("parsing.requests.websocket")
                .description("Number of WebSocket parsing requests")
                .register(registry);
    }

    // REST: Запуск парсинга (симуляция)
    @PostMapping("/start")
    @Timed(value = "parsing.start.polling", description = "Time to start polling parsing")
    public ResponseEntity<ParseJob> startParsing(@RequestParam(defaultValue = "5") int delay) {
        pollingRequestsCounter.increment();

        String jobId = UUID.randomUUID().toString();
        ParseJob job = new ParseJob(jobId);
        jobs.put(jobId, job);

        // Симуляция парсинга в отдельном потоке
        executor.submit(() -> performParsing(job, delay));

        return ResponseEntity.ok(job);
    }

    // REST: Проверка статуса (для polling)
    @GetMapping("/status/{jobId}")
    public ResponseEntity<ParseJob> getStatus(@PathVariable String jobId) {
        ParseJob job = jobs.get(jobId);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(job);
    }

    // WebSocket: Запуск парсинга с отправкой через WS
    @PostMapping("/start-ws")
    @Timed(value = "parsing.start.websocket", description = "Time to start WebSocket parsing")
    public ResponseEntity<ParseJob> startParsingWebSocket(@RequestParam(defaultValue = "5") int delay) {
        wsRequestsCounter.increment();

        String jobId = UUID.randomUUID().toString();
        ParseJob job = new ParseJob(jobId);
        jobs.put(jobId, job);

        // Симуляция парсинга с уведомлением через WebSocket
        executor.submit(() -> {
            performParsing(job, delay);
            // Отправляем результат через WebSocket
            messagingTemplate.convertAndSend("/topic/parsing/" + jobId, job);
        });

        return ResponseEntity.ok(job);
    }

    // НОВЫЙ: Реальный парсинг из файла с метриками
    @PostMapping("/parse-file")
    @Timed(value = "parsing.file", description = "Time to parse file")
    public ResponseEntity<Map<String, Object>> parseFileWithMetrics(
            @RequestParam String filename) {

        long startTime = System.currentTimeMillis();

        // Используем сервис с метриками
        TaskParsingService.ParseResult result =
                parsingService.parseAndSaveTasksFromFile(filename);

        long duration = System.currentTimeMillis() - startTime;

        // Записываем в метрики Micrometer
        Timer.builder("parsing.file.duration")
                .description("File parsing duration")
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);

        return ResponseEntity.ok(Map.of(
                "success", result.success,
                "recordsParsed", result.recordsParsed,
                "recordsSaved", result.recordsSaved,
                "durationMs", duration,
                "throughput", result.recordsSaved * 1000.0 / duration, // records/sec
                "error", result.errorMessage != null ? result.errorMessage : ""
        ));
    }

    // НОВЫЙ: Парсинг файла с WebSocket уведомлением
    @PostMapping("/parse-file-ws")
    public ResponseEntity<ParseJob> parseFileWebSocket(
            @RequestParam String filename) {

        String jobId = UUID.randomUUID().toString();
        ParseJob job = new ParseJob(jobId);
        jobs.put(jobId, job);

        executor.submit(() -> {
            try {
                job.setStatus("RUNNING");
                job.setResult("Parsing file: " + filename);

                // Реальный парсинг
                TaskParsingService.ParseResult result =
                        parsingService.parseAndSaveTasksFromFile(filename);

                if (result.success) {
                    job.setStatus("COMPLETED");
                    job.setResult(String.format(
                            "Parsed %d records, saved %d to database",
                            result.recordsParsed, result.recordsSaved
                    ));
                } else {
                    job.setStatus("FAILED");
                    job.setResult("Error: " + result.errorMessage);
                }

            } catch (Exception e) {
                job.setStatus("FAILED");
                job.setResult("Error: " + e.getMessage());
            } finally {
                job.setEndTime(System.currentTimeMillis());
                // Отправляем через WebSocket
                messagingTemplate.convertAndSend("/topic/parsing/" + jobId, job);
            }
        });

        return ResponseEntity.ok(job);
    }

    // НОВЫЙ: Endpoint для нагрузочного тестирования
    @PostMapping("/benchmark")
    public ResponseEntity<Map<String, Object>> runBenchmark(
            @RequestParam(defaultValue = "test-data-10k.txt") String filename,
            @RequestParam(defaultValue = "10") int iterations) {

        long totalTime = 0;
        int totalRecords = 0;
        int successCount = 0;

        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();

            TaskParsingService.ParseResult result =
                    parsingService.parseAndSaveTasksFromFile(filename);

            long duration = System.currentTimeMillis() - start;
            totalTime += duration;

            if (result.success) {
                successCount++;
                totalRecords += result.recordsSaved;
            }
        }

        double avgTime = totalTime / (double) iterations;
        double throughput = (totalRecords * 1000.0) / totalTime;

        return ResponseEntity.ok(Map.of(
                "iterations", iterations,
                "successCount", successCount,
                "totalRecords", totalRecords,
                "totalTimeMs", totalTime,
                "avgTimeMs", avgTime,
                "throughput", throughput,
                "recordsPerSecond", throughput
        ));
    }

    // Симуляция долгого парсинга (старый метод)
    private void performParsing(ParseJob job, int delaySeconds) {
        try {
            Thread.sleep(delaySeconds * 1000L);

            job.setStatus("COMPLETED");
            job.setResult("Parsed 150 items successfully at " + System.currentTimeMillis());
            job.setEndTime(System.currentTimeMillis());

        } catch (InterruptedException e) {
            job.setStatus("FAILED");
            job.setResult("Error: " + e.getMessage());
            job.setEndTime(System.currentTimeMillis());
        }
    }
}