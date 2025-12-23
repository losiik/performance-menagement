package ru.klosep.performance_management.port;

import ru.klosep.performance_management.model.Task;
import java.util.List;

public interface TaskDataSource {
    List<Task> fetchTasks();
}