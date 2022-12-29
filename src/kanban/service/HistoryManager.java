package kanban.service;

import java.util.ArrayList;

public interface HistoryManager {
    ArrayList<Integer> getHistory();

    void add(int taskId);
}
