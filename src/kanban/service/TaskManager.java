package kanban.service;

import kanban.model.*;
import java.util.ArrayList;

public interface TaskManager {
    ArrayList<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskByTaskId(int taskId);

    int createTask(Task task);

    int updateTask(Task task);

    int deleteTaskByTaskId(int taskId);

    ArrayList<SubTask> getAllSubTasksByEpicTask(Task task);

    ArrayList<Task> getHistory();
}