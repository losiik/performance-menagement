package ru.klosep.performance_management.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/virtual")
public class VirtualThreadController {

    @GetMapping("/process")
    public String processWithVirtualThreads() throws InterruptedException {
        // Симулируем долгую операцию
        Thread.sleep(100);
        return "Processed with Virtual Thread: " + Thread.currentThread();
    }

    @GetMapping("/heavy")
    public String heavyOperation() throws InterruptedException {
        // Более тяжёлая операция
        Thread.sleep(500);
        return "Heavy operation completed with: " + Thread.currentThread();
    }
}