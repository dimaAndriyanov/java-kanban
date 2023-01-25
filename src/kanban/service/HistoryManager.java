package kanban.service;

import java.util.List;
import kanban.model.Task;

public interface HistoryManager {
    List<Task> getHistory();

    void add(Task task);

    void remove(int taskId);

    void clear();
}