package ru.klosep.performance_management.service;

import org.junit.jupiter.api.Test;
import ru.klosep.performance_management.model.Task;
import ru.klosep.performance_management.model.TaskAnalysis;
import ru.klosep.performance_management.port.AnalysisResultPublisher;
import ru.klosep.performance_management.port.TaskDataSource;
import ru.klosep.performance_management.service.TaskAnalysisService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskAnalysisServiceTest {

    @Test
    void shouldAnalyzeTasksCorrectly() {
        // Mock адаптеры (без Spring, без реальных зависимостей)
        TaskDataSource mockDataSource = () -> List.of(
                new Task(1L, "Task 1", "Desc 1", true),
                new Task(2L, "Task 2", "Desc 2", false),
                new Task(3L, "Task 3", "Desc 3", true),
                new Task(4L, "Task 4", "Desc 4", true)
        );

        List<TaskAnalysis> capturedResults = new ArrayList<>();
        AnalysisResultPublisher mockPublisher = capturedResults::add;

        // Создаём сервис с моками
        TaskAnalysisService service = new TaskAnalysisService(
                mockDataSource, mockPublisher
        );

        // Выполняем бизнес-логику
        service.analyzeAndPublish();

        // Проверяем результаты
        assertEquals(1, capturedResults.size());
        TaskAnalysis result = capturedResults.get(0);

        assertEquals(4, result.getTotalTasks());
        assertEquals(3, result.getCompletedTasks());
        assertEquals(0.75, result.getCompletionRate(), 0.01);
        assertTrue(result.getAverageTitleLength() > 0);
        assertFalse(result.getRecommendations().isEmpty());
    }

    @Test
    void shouldHandleEmptyTaskList() {
        TaskDataSource emptySource = List::of;
        List<TaskAnalysis> results = new ArrayList<>();
        AnalysisResultPublisher publisher = results::add;

        TaskAnalysisService service = new TaskAnalysisService(emptySource, publisher);
        service.analyzeAndPublish();

        TaskAnalysis result = results.get(0);
        assertEquals(0, result.getTotalTasks());
        assertTrue(result.getRecommendations().contains("Нет задач для анализа"));
    }

    @Test
    void shouldGenerateCorrectRecommendations() {
        TaskDataSource lowCompletionSource = () -> List.of(
                new Task(1L, "A", "Desc", false),
                new Task(2L, "B", "Desc", false),
                new Task(3L, "C", "Desc", true)
        );

        List<TaskAnalysis> results = new ArrayList<>();
        TaskAnalysisService service = new TaskAnalysisService(
                lowCompletionSource, results::add
        );

        service.analyzeAndPublish();
        TaskAnalysis result = results.get(0);

        assertTrue(result.getRecommendations().stream()
                .anyMatch(r -> r.contains("Низкий процент выполнения")));
    }
}