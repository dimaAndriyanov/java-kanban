package kanban.service;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(1, getDefaultHistory());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}