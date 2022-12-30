package kanban.service;

import java.util.List;

public interface HistoryManager {
    List<Integer> getHistory();

    void add(int taskId);
}