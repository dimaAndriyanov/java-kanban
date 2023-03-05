package kanban.service;

import kanban.exceptions.HistoryManagerException;
import kanban.model.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

abstract class HistoryManagerTest<T extends HistoryManager> {
    public HistoryManager manager;

    public void setManager(T manager) {
        this.manager = manager;
    }

    @Test
    public void getHistory() {
        assertNotNull(manager.getHistory());
        assertTrue(manager.getHistory().isEmpty());

        Task task1 = new Task("a", "b");
        Task task2 = new Task("c", "d");
        Task task3 = new Task("e", "f");

        task1.setTaskId(1);
        task2.setTaskId(2);
        task3.setTaskId(3);

        manager.add(task1);
        manager.add(task2);
        manager.add(task3);

        assertArrayEquals(new Task[] {task1, task2, task3}, manager.getHistory().toArray());
    }

    @Test
    public void add() {
        HistoryManagerException exception = assertThrows(HistoryManagerException.class, () -> manager.add(null));
        assertEquals("Can not add null Task to history", exception.getMessage());

        Task task1 = new Task("a", "b");
        task1.setTaskId(1);
        manager.add(task1);
        assertNotNull(manager.getHistory().get(0));

        Task task2 = new Task("c", "d");
        Task task3 = new Task("e", "f");

        task2.setTaskId(2);
        task3.setTaskId(3);

        manager.add(task2);
        manager.add(task3);
        manager.add(task1);

        assertArrayEquals(new Task[] {task2, task3, task1}, manager.getHistory().toArray());
    }

    @Test
    public void remove() {
        Task task1 = new Task("a", "b");
        Task task2 = new Task("c", "d");
        Task task3 = new Task("e", "f");
        Task task4 = new Task("g", "h");
        Task task5 = new Task("i", "j");

        task1.setTaskId(1);
        task2.setTaskId(2);
        task3.setTaskId(3);
        task4.setTaskId(4);
        task5.setTaskId(5);

        manager.add(task1);
        manager.add(task2);
        manager.add(task3);
        manager.add(task4);
        manager.add(task5);

        manager.remove(1);
        manager.remove(3);
        manager.remove(5);

        assertArrayEquals(new Task[] {task2, task4}, manager.getHistory().toArray());
    }

    @Test
    public void clear() {
        Task task1 = new Task("a", "b");
        Task task2 = new Task("c", "d");
        Task task3 = new Task("e", "f");

        task1.setTaskId(1);
        task2.setTaskId(2);
        task3.setTaskId(3);

        manager.add(task1);
        manager.add(task2);
        manager.add(task3);

        assertFalse(manager.getHistory().isEmpty());

        manager.clear();

        assertNotNull(manager.getHistory());
        assertTrue(manager.getHistory().isEmpty());
    }
}