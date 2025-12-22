package ru.klosep.performance_management.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/memory")
public class MemoryLeakController {

    // Статическая коллекция - создаёт утечку памяти
    private static final List<byte[]> MEMORY_LEAK = new ArrayList<>();

    @GetMapping("/leak")
    public String createLeak() {
        // Добавляем 10 MB данных при каждом запросе
        for (int i = 0; i < 10; i++) {
            byte[] data = new byte[1024 * 1024]; // 1 MB
            MEMORY_LEAK.add(data);
        }
        return "Added 10 MB to memory. Total size: " + MEMORY_LEAK.size() + " MB";
    }

    @GetMapping("/leak-objects")
    public String createObjectLeak() {
        // Создаём утечку из объектов
        for (int i = 0; i < 1000; i++) {
            MEMORY_LEAK.add(UUID.randomUUID().toString().getBytes());
        }
        return "Added 1000 objects. Total objects: " + MEMORY_LEAK.size();
    }

    @GetMapping("/status")
    public String getStatus() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);

        return String.format("Used: %d MB, Max: %d MB, Leak size: %d MB",
                usedMemory, maxMemory, MEMORY_LEAK.size());
    }
}
