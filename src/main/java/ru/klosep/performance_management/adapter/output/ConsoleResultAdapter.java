package ru.klosep.performance_management.adapter.output;

import org.springframework.stereotype.Component;
import ru.klosep.performance_management.model.TaskAnalysis;
import ru.klosep.performance_management.port.AnalysisResultPublisher;

@Component("consoleResultAdapter")
public class ConsoleResultAdapter implements AnalysisResultPublisher {

    @Override
    public void publish(TaskAnalysis analysis) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 АНАЛИЗ ЗАДАЧ (Console Output)");
        System.out.println("=".repeat(60));
        System.out.println("Всего задач: " + analysis.getTotalTasks());
        System.out.println("Выполнено: " + analysis.getCompletedTasks());
        System.out.println(String.format("Процент выполнения: %.1f%%",
                analysis.getCompletionRate() * 100));
        System.out.println(String.format("Средняя длина названия: %.1f символов",
                analysis.getAverageTitleLength()));
        System.out.println("\n💡 Рекомендации:");
        analysis.getRecommendations().forEach(r -> System.out.println("  • " + r));
        System.out.println("=".repeat(60) + "\n");
    }
}
