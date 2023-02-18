package kanban.service;

import kanban.model.*;
import kanban.exceptions.*;
import java.util.List;

public interface TaskManager {
    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskByTaskId(int taskId) throws TaskManagerException;

    int createTask(Task task) throws TaskManagerException;

    int updateTask(Task task) throws TaskManagerException;

    int deleteTaskByTaskId(int taskId) throws TaskManagerException;

    List<SubTask> getAllSubTasksByEpicTask(int taskId) throws TaskManagerException;

    List<Task> getHistory();
}