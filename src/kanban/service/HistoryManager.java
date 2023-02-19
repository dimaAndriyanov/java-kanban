package kanban.service;

import java.util.List;

import kanban.exceptions.HistoryManagerException;
import kanban.model.Task;

public interface HistoryManager {
    List<Task> getHistory();

    void add(Task task) throws HistoryManagerException;

    void remove(int taskId);

    void clear();
}