package ru.klosep.performance_management.controller;

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
        // Добавляем тестовые данные
        for (int i = 1; i <= 5; i++) {
            Task task = new Task((long) i, "Task " + i, "Description " + i, false);
            tasks.put((long) i, task);
            counter.set(i + 1);
        }
    }

    // CREATE - создание задачи
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        // Симулируем задержку БД
        simulateDelay(50);

        Long id = counter.getAndIncrement();
        task.setId(id);
        tasks.put(id, task);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    // READ - получение всех задач
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        simulateDelay(30);
        return ResponseEntity.ok(new ArrayList<>(tasks.values()));
    }

    // READ - получение одной задачи
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        simulateDelay(20);
        Task task = tasks.get(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    // UPDATE - обновление задачи
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskDetails) {
        simulateDelay(60);

        Task task = tasks.get(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }

        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setCompleted(taskDetails.isCompleted());

        return ResponseEntity.ok(task);
    }

    // DELETE - удаление задачи
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        simulateDelay(40);

        Task removed = tasks.remove(id);
        if (removed == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // Симуляция задержки БД/внешних сервисов
    private void simulateDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
