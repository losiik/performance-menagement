package ru.klosep.performance_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.klosep.performance_management.model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final ConcurrentHashMap<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    public TaskController() {
        // Инициализация тестовых данных
        for (int i = 1; i <= 100; i++) {
            Task task = new Task((long) i, "Task " + i, "Description " + i, false);
            tasks.put((long) i, task);
            counter.set(i + 1);
        }
    }

    // ГОРЯЧИЕ ДАННЫЕ: Получение всех задач (часто запрашивается, редко меняется)
    @Cacheable(value = "tasks", key = "'all'")
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        System.out.println("⚠️ CACHE MISS: Loading all tasks from storage...");
        simulateDbDelay(200); // Симуляция запроса к БД
        return ResponseEntity.ok(new ArrayList<>(tasks.values()));
    }

    // ГОРЯЧИЕ ДАННЫЕ: Получение одной задачи по ID
    @Cacheable(value = "task", key = "#id")
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        System.out.println("⚠️ CACHE MISS: Loading task " + id + " from storage...");
        simulateDbDelay(100);
        Task task = tasks.get(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    // CREATE - с инвалидацией кэша всех задач
    @CacheEvict(value = "tasks", key = "'all'")
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        System.out.println("✅ Cache evicted: 'tasks:all'");
        simulateDbDelay(50);
        Long id = counter.getAndIncrement();
        task.setId(id);
        tasks.put(id, task);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    // UPDATE - с инвалидацией обоих кэшей
    @CacheEvict(value = {"task", "tasks"}, key = "#id")
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskDetails) {
        System.out.println("✅ Cache evicted: task:" + id + " and tasks:all");
        simulateDbDelay(80);

        Task task = tasks.get(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }

        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setCompleted(taskDetails.isCompleted());

        return ResponseEntity.ok(task);
    }

    // DELETE - с инвалидацией кэшей
    @CacheEvict(value = {"task", "tasks"}, allEntries = true)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        System.out.println("✅ Cache evicted: all entries");
        simulateDbDelay(40);

        Task removed = tasks.remove(id);
        if (removed == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // Endpoint для тестирования производительности
    @GetMapping("/benchmark")
    public ResponseEntity<String> benchmark() {
        long start = System.nanoTime();
        getAllTasks();
        long duration = (System.nanoTime() - start) / 1_000_000;
        return ResponseEntity.ok("Response time: " + duration + " ms");
    }

    private void simulateDbDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}