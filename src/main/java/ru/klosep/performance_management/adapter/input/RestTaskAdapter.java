package ru.klosep.performance_management.adapter.input;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.klosep.performance_management.model.Task;
import ru.klosep.performance_management.port.TaskDataSource;

import java.util.Arrays;
import java.util.List;

@Component("restTaskAdapter")
public class RestTaskAdapter implements TaskDataSource {

    private final RestTemplate restTemplate;
    private final String apiUrl;

    public RestTaskAdapter() {
        this.restTemplate = new RestTemplate();
        this.apiUrl = "http://localhost:8080/api/tasks";
    }

    @Override
    public List<Task> fetchTasks() {
        System.out.println("📡 Fetching tasks from REST API: " + apiUrl);
        try {
            Task[] tasks = restTemplate.getForObject(apiUrl, Task[].class);
            return tasks != null ? Arrays.asList(tasks) : List.of();
        } catch (Exception e) {
            System.err.println("❌ Error fetching from API: " + e.getMessage());
            return List.of();
        }
    }
}