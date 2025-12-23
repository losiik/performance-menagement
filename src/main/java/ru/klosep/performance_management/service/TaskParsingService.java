package ru.klosep.performance_management.service;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.klosep.performance_management.model.Task;
import ru.klosep.performance_management.postgres.entity.TaskEntity;
import ru.klosep.performance_management.postgres.repository.TaskJpaRepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TaskParsingService {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private TaskJpaRepository taskRepository;

    // Кастомные метрики
    private final Counter successCounter;
    private final Counter errorCounter;
    private final Counter recordsCounter;
    private final Timer parsingTimer;
    private final AtomicInteger currentlyParsing = new AtomicInteger(0);

    public TaskParsingService(MeterRegistry registry) {
        this.successCounter = Counter.builder("task.parsing.success")
                .description("Successful parsing operations")
                .register(registry);

        this.errorCounter = Counter.builder("task.parsing.errors")
                .description("Failed parsing operations")
                .register(registry);

        this.recordsCounter = Counter.builder("task.parsing.records")
                .description("Total records parsed")
                .register(registry);

        this.parsingTimer = Timer.builder("task.parsing.duration")
                .description("Time taken to parse tasks")
                .register(registry);

        Gauge.builder("task.parsing.active", currentlyParsing, AtomicInteger::get)
                .description("Currently active parsing operations")
                .register(registry);

        Gauge.builder("task.parsing.memory.used",
                        () -> (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024.0 * 1024.0))
                .description("Memory used during parsing (MB)")
                .register(registry);
    }

    @Timed(value = "task.parsing.file.operation", description = "Complete file parsing operation")
    public ParseResult parseAndSaveTasksFromFile(String filename) {
        currentlyParsing.incrementAndGet();

        try {
            return parsingTimer.record(() -> {
                try {
                    // Этап 1: Чтение файла
                    List<Task> tasks = readTasksFromFile(filename);

                    // Этап 2: Валидация
                    List<Task> validTasks = validateTasks(tasks);

                    // Этап 3: Сохранение в БД
                    int savedCount = saveTasksToDB(validTasks);

                    // Обновляем метрики
                    successCounter.increment();
                    recordsCounter.increment(savedCount);

                    return new ParseResult(true, validTasks.size(), savedCount, null);

                } catch (Exception e) {
                    errorCounter.increment();
                    return new ParseResult(false, 0, 0, e.getMessage());
                }
            });
        } finally {
            currentlyParsing.decrementAndGet();
        }
    }

    @Timed(value = "task.parsing.read", description = "Read tasks from file")
    private List<Task> readTasksFromFile(String filename) throws Exception {
        List<Task> tasks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine(); // Пропускаем заголовок

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    Task task = new Task(
                            Long.parseLong(parts[0].trim()),
                            parts[1].trim().replace("\"", ""),
                            parts[2].trim().replace("\"", ""),
                            Boolean.parseBoolean(parts[3].trim())
                    );
                    tasks.add(task);
                }
            }
        }

        return tasks;
    }

    @Timed(value = "task.parsing.validate", description = "Validate tasks")
    private List<Task> validateTasks(List<Task> tasks) {
        return tasks.stream()
                .filter(t -> t.getTitle() != null && !t.getTitle().isEmpty())
                .toList();
    }

    @Timed(value = "task.parsing.save", description = "Save tasks to database")
    private int saveTasksToDB(List<Task> tasks) {
        List<TaskEntity> entities = new ArrayList<>();

        for (Task task : tasks) {
            TaskEntity entity = new TaskEntity(
                    task.getTitle(),
                    task.getDescription(),
                    task.isCompleted()
            );
            entities.add(entity);
        }

        // Batch insert (оптимизация!)
        taskRepository.saveAll(entities);

        return entities.size();
    }

    public static class ParseResult {
        public final boolean success;
        public final int recordsParsed;
        public final int recordsSaved;
        public final String errorMessage;

        public ParseResult(boolean success, int recordsParsed, int recordsSaved, String errorMessage) {
            this.success = success;
            this.recordsParsed = recordsParsed;
            this.recordsSaved = recordsSaved;
            this.errorMessage = errorMessage;
        }
    }
}