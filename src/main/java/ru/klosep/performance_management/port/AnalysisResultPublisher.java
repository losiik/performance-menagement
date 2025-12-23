package ru.klosep.performance_management.port;

import ru.klosep.performance_management.model.TaskAnalysis;

// Выходной порт - публикация результатов
public interface AnalysisResultPublisher {
    void publish(TaskAnalysis analysis);
}