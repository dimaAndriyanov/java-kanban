package kanban.service;

import kanban.model.*;
import java.util.List;

public interface TaskManager {
    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskByTaskId(int taskId);

    int createTask(Task task);

    int updateTask(Task task);

    int deleteTaskByTaskId(int taskId);

    List<SubTask> getAllSubTasksByEpicTask(Task task);

    List<Task> getHistory();
}