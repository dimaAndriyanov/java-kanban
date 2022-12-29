package kanban.service;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager{
    private final ArrayList<Integer> watchedTasks = new ArrayList<>();

    @Override
    public ArrayList<Integer> getHistory() {
        ArrayList<Integer> history = new ArrayList<>();
        if (watchedTasks.isEmpty()) {
            return history;
        }
        for (int i = 0; i < Math.min(10, watchedTasks.size()); i++) {
            history.add(watchedTasks.get(watchedTasks.size() - 1 - i));
        }
        return history;
    }

    @Override
    public void add(int taskId) {
        watchedTasks.add(taskId);
    }
}
