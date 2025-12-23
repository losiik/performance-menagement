package ru.klosep.performance_management.adapter.output;

import org.springframework.stereotype.Component;
import ru.klosep.performance_management.model.TaskAnalysis;
import ru.klosep.performance_management.port.AnalysisResultPublisher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component("databaseResultAdapter")
public class DatabaseResultAdapter implements AnalysisResultPublisher {

    // Симуляция БД - в реальности тут был бы JpaRepository
    private final List<String> database = new ArrayList<>();

    @Override
    public void publish(TaskAnalysis analysis) {
        String record = String.format(
                "[%s] Analysis: total=%d, completed=%d, rate=%.2f",
                LocalDateTime.now(),
                analysis.getTotalTasks(),
                analysis.getCompletedTasks(),
                analysis.getCompletionRate()
        );

        database.add(record);
        System.out.println("💾 Saved to database: " + record);
        System.out.println("   Total records in DB: " + database.size());
    }

    public List<String> getAllRecords() {
        return new ArrayList<>(database);
    }
}
