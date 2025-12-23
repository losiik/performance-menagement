package ru.klosep.performance_management.service;

import ru.klosep.performance_management.model.Task;
import ru.klosep.performance_management.model.TaskAnalysis;
import ru.klosep.performance_management.port.TaskDataSource;
import ru.klosep.performance_management.port.AnalysisResultPublisher;

import java.util.ArrayList;
import java.util.List;

public class TaskAnalysisService {

    private final TaskDataSource dataSource;
    private final AnalysisResultPublisher publisher;

    public TaskAnalysisService(TaskDataSource dataSource,
                               AnalysisResultPublisher publisher) {
        this.dataSource = dataSource;
        this.publisher = publisher;
    }

    // Ядро бизнес-логики: анализ задач
    public void analyzeAndPublish() {
        List<Task> tasks = dataSource.fetchTasks();
        TaskAnalysis analysis = performAnalysis(tasks);
        publisher.publish(analysis);
    }

    // Чистая функция расчёта
    private TaskAnalysis performAnalysis(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return new TaskAnalysis(0, 0, 0.0, 0.0,
                    List.of("Нет задач для анализа"));
        }

        int total = tasks.size();
        int completed = (int) tasks.stream()
                .filter(Task::isCompleted)
                .count();

        double completionRate = (double) completed / total;

        double avgTitleLength = tasks.stream()
                .mapToInt(t -> t.getTitle().length())
                .average()
                .orElse(0.0);

        List<String> recommendations = generateRecommendations(
                completionRate, avgTitleLength
        );

        return new TaskAnalysis(total, completed, completionRate,
                avgTitleLength, recommendations);
    }

    private List<String> generateRecommendations(double rate, double avgLength) {
        List<String> recommendations = new ArrayList<>();

        if (rate < 0.5) {
            recommendations.add("Низкий процент выполнения (<50%). Рекомендуется разбить задачи на меньшие подзадачи.");
        } else if (rate > 0.9) {
            recommendations.add("Отличная продуктивность! Можно увеличить сложность задач.");
        }

        if (avgLength > 100) {
            recommendations.add("Слишком длинные названия задач. Сократите до 50-60 символов.");
        } else if (avgLength < 10) {
            recommendations.add("Слишком короткие названия. Добавьте больше контекста.");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Всё в норме. Продолжайте в том же духе!");
        }

        return recommendations;
    }
}