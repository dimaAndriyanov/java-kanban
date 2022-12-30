package kanban.service;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager{
    private final List<Integer> watchedTasks = new LinkedList<>();
    private static final int HISTORY_CAPACITY = 10;

    @Override
    public List<Integer> getHistory() {
        ArrayList<Integer> history = new ArrayList<>(HISTORY_CAPACITY);
        for (int i = watchedTasks.size(); i > 0; i--) {
            history.add(watchedTasks.get(i - 1));
        }
        return history;
    }

    @Override
    public void add(int taskId) {
        if (watchedTasks.size() == HISTORY_CAPACITY) {
            watchedTasks.remove(0);
            watchedTasks.add(taskId);
        } else {
            watchedTasks.add(taskId);
        }
    }
}