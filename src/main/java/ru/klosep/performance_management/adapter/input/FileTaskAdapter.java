package ru.klosep.performance_management.adapter.input;

import org.springframework.stereotype.Component;
import ru.klosep.performance_management.model.Task;
import ru.klosep.performance_management.port.TaskDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component("fileTaskAdapter")
public class FileTaskAdapter implements TaskDataSource {

    private final String filePath;

    public FileTaskAdapter() {
        this.filePath = "tasks.csv";
    }

    @Override
    public List<Task> fetchTasks() {
        System.out.println("📁 Reading tasks from file: " + filePath);
        List<Task> tasks = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Path.of(filePath));
            for (int i = 1; i < lines.size(); i++) { // Пропускаем заголовок
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 4) {
                    Task task = new Task(
                            Long.parseLong(parts[0].trim()),
                            parts[1].trim(),
                            parts[2].trim(),
                            Boolean.parseBoolean(parts[3].trim())
                    );
                    tasks.add(task);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error reading file: " + e.getMessage());
            // Возвращаем тестовые данные если файл не найден
            return createMockTasks();
        }

        return tasks;
    }

    private List<Task> createMockTasks() {
        return List.of(
                new Task(1L, "Implement feature A", "Description A", true),
                new Task(2L, "Fix bug B", "Description B", false),
                new Task(3L, "Write tests", "Description C", true)
        );
    }
}
