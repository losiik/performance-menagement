package ru.klosep.performance_management.model;

import java.util.List;

public class TaskAnalysis {
    private final int totalTasks;
    private final int completedTasks;
    private final double completionRate;
    private final double averageTitleLength;
    private final List<String> recommendations;

    public TaskAnalysis(int totalTasks, int completedTasks,
                        double completionRate, double averageTitleLength,
                        List<String> recommendations) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.completionRate = completionRate;
        this.averageTitleLength = averageTitleLength;
        this.recommendations = recommendations;
    }

    // Геттеры
    public int getTotalTasks() { return totalTasks; }
    public int getCompletedTasks() { return completedTasks; }
    public double getCompletionRate() { return completionRate; }
    public double getAverageTitleLength() { return averageTitleLength; }
    public List<String> getRecommendations() { return recommendations; }

    @Override
    public String toString() {
        return String.format(
                "TaskAnalysis{total=%d, completed=%d, rate=%.2f%%, avgLength=%.1f}",
                totalTasks, completedTasks, completionRate * 100, averageTitleLength
        );
    }
}
