package ru.klosep.performance_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import ru.klosep.performance_management.model.ParseJob;

import java.util.UUID;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api/parse")
public class ParsingController {

    private final ConcurrentHashMap<String, ParseJob> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // REST: Запуск парсинга
    @PostMapping("/start")
    public ResponseEntity<ParseJob> startParsing(@RequestParam(defaultValue = "5") int delay) {
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
    public ResponseEntity<ParseJob> startParsingWebSocket(@RequestParam(defaultValue = "5") int delay) {
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

    // Симуляция долгого парсинга
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