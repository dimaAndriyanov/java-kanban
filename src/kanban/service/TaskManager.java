package kanban.service;

import kanban.model.*;

import java.util.ArrayList;

public interface TaskManager {
    public ArrayList<Task> getAllTasks();

    public void deleteAllTasks();

    public Task getTaskByTaskId(int taskId);

    public int createTask(Task task);

    public int updateTask(Task task);

    public int deleteTaskByTaskId(int taskId);

    public ArrayList<SubTask> getAllSubTasksByEpicTask(Task task);
}