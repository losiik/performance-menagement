package ru.klosep.performance_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.klosep.performance_management.mongo.document.TaskDocument;
import ru.klosep.performance_management.mongo.repository.TaskMongoRepository;
import ru.klosep.performance_management.postgres.entity.TaskEntity;
import ru.klosep.performance_management.postgres.repository.TaskJpaRepository;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/benchmark")
public class DatabaseBenchmarkController {

    @Autowired
    private TaskJpaRepository postgresRepo;

    @Autowired
    private TaskMongoRepository mongoRepo;

    // === ВСТАВКА ОДНОЙ ЗАПИСИ ===

    @PostMapping("/postgres/insert-one")
    public ResponseEntity<Map<String, Object>> insertOnePostgres() {
        long start = System.nanoTime();

        TaskEntity task = new TaskEntity(
                "Test task " + UUID.randomUUID(),
                "Description for benchmark",
                false
        );
        postgresRepo.save(task);

        long duration = (System.nanoTime() - start) / 1_000_000; // ms

        return ResponseEntity.ok(Map.of(
                "database", "PostgreSQL",
                "operation", "INSERT ONE",
                "duration_ms", duration,
                "id", task.getId()
        ));
    }

    @PostMapping("/mongo/insert-one")
    public ResponseEntity<Map<String, Object>> insertOneMongo() {
        long start = System.nanoTime();

        TaskDocument task = new TaskDocument(
                "Test task " + UUID.randomUUID(),
                "Description for benchmark",
                false
        );
        mongoRepo.save(task);

        long duration = (System.nanoTime() - start) / 1_000_000;

        return ResponseEntity.ok(Map.of(
                "database", "MongoDB",
                "operation", "INSERT ONE",
                "duration_ms", duration,
                "id", task.getId()
        ));
    }

    // === ПАКЕТНАЯ ВСТАВКА ===

    @PostMapping("/postgres/insert-batch")
    public ResponseEntity<Map<String, Object>> insertBatchPostgres(
            @RequestParam(defaultValue = "1000") int count) {

        List<TaskEntity> tasks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            tasks.add(new TaskEntity(
                    "Batch task " + i,
                    "Description " + i,
                    i % 3 == 0 // Каждая 3-я выполнена
            ));
        }

        long start = System.nanoTime();
        postgresRepo.saveAll(tasks);
        long duration = (System.nanoTime() - start) / 1_000_000;

        return ResponseEntity.ok(Map.of(
                "database", "PostgreSQL",
                "operation", "BATCH INSERT",
                "count", count,
                "duration_ms", duration,
                "throughput_ops", (count * 1000.0) / duration
        ));
    }

    @PostMapping("/mongo/insert-batch")
    public ResponseEntity<Map<String, Object>> insertBatchMongo(
            @RequestParam(defaultValue = "1000") int count) {

        List<TaskDocument> tasks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            tasks.add(new TaskDocument(
                    "Batch task " + i,
                    "Description " + i,
                    i % 3 == 0
            ));
        }

        long start = System.nanoTime();
        mongoRepo.saveAll(tasks);
        long duration = (System.nanoTime() - start) / 1_000_000;

        return ResponseEntity.ok(Map.of(
                "database", "MongoDB",
                "operation", "BATCH INSERT",
                "count", count,
                "duration_ms", duration,
                "throughput_ops", (count * 1000.0) / duration
        ));
    }

    // === ЧТЕНИЕ ПО КЛЮЧУ ===

    @GetMapping("/postgres/read-one/{id}")
    public ResponseEntity<Map<String, Object>> readOnePostgres(@PathVariable Long id) {
        long start = System.nanoTime();
        Optional<TaskEntity> task = postgresRepo.findById(id);
        long duration = (System.nanoTime() - start) / 1_000_000;

        return ResponseEntity.ok(Map.of(
                "database", "PostgreSQL",
                "operation", "READ BY ID",
                "duration_ms", duration,
                "found", task.isPresent()
        ));
    }

    @GetMapping("/mongo/read-one/{id}")
    public ResponseEntity<Map<String, Object>> readOneMongo(@PathVariable String id) {
        long start = System.nanoTime();
        Optional<TaskDocument> task = mongoRepo.findById(id);
        long duration = (System.nanoTime() - start) / 1_000_000;

        return ResponseEntity.ok(Map.of(
                "database", "MongoDB",
                "operation", "READ BY ID",
                "duration_ms", duration,
                "found", task.isPresent()
        ));
    }

    // === ФИЛЬТРАЦИЯ ===

    @GetMapping("/postgres/filter-completed")
    public ResponseEntity<Map<String, Object>> filterCompletedPostgres() {
        long start = System.nanoTime();
        List<TaskEntity> tasks = postgresRepo.findByCompleted(true);
        long duration = (System.nanoTime() - start) / 1_000_000;

        return ResponseEntity.ok(Map.of(
                "database", "PostgreSQL",
                "operation", "FILTER BY COMPLETED",
                "duration_ms", duration,
                "count", tasks.size()
        ));
    }

    @GetMapping("/mongo/filter-completed")
    public ResponseEntity<Map<String, Object>> filterCompletedMongo() {
        long start = System.nanoTime();
        List<TaskDocument> tasks = mongoRepo.findByCompleted(true);
        long duration = (System.nanoTime() - start) / 1_000_000;

        return ResponseEntity.ok(Map.of(
                "database", "MongoDB",
                "operation", "FILTER BY COMPLETED",
                "duration_ms", duration,
                "count", tasks.size()
        ));
    }

    @GetMapping("/postgres/filter-week")
    public ResponseEntity<Map<String, Object>> filterWeekPostgres() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        long start = System.nanoTime();
        List<TaskEntity> tasks = postgresRepo.findByCreatedAtBetween(
                weekAgo, LocalDateTime.now());
        long duration = (System.nanoTime() - start) / 1_000_000;

        return ResponseEntity.ok(Map.of(
                "database", "PostgreSQL",
                "operation", "FILTER LAST WEEK",
                "duration_ms", duration,
                "count", tasks.size()
        ));
    }

    @GetMapping("/mongo/filter-week")
    public ResponseEntity<Map<String, Object>> filterWeekMongo() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        long start = System.nanoTime();
        List<TaskDocument> tasks = mongoRepo.findByCreatedAtBetween(
                weekAgo, LocalDateTime.now());
        long duration = (System.nanoTime() - start) / 1_000_000;

        return ResponseEntity.ok(Map.of(
                "database", "MongoDB",
                "operation", "FILTER LAST WEEK",
                "duration_ms", duration,
                "count", tasks.size()
        ));
    }

    // === ОЧИСТКА ===

    @DeleteMapping("/postgres/clear")
    public ResponseEntity<String> clearPostgres() {
        postgresRepo.deleteAll();
        return ResponseEntity.ok("PostgreSQL cleared");
    }

    @DeleteMapping("/mongo/clear")
    public ResponseEntity<String> clearMongo() {
        mongoRepo.deleteAll();
        return ResponseEntity.ok("MongoDB cleared");
    }
}
