package kanban.service;

import kanban.exceptions.TaskTimeException;
import kanban.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    public void setManager() {
        setManager(Managers.getInMemoryTaskManager());
    }

    @Test
    public void updateEpicTaskStatus() {
        manager.createTask(new EpicTask("a", "b"));
        assertEquals(TaskStatus.NEW, manager.getTaskByTaskId(1).getStatus());

        manager.createTask(new SubTask("c", "d", 1));
        assertEquals(TaskStatus.NEW, manager.getTaskByTaskId(1).getStatus());

        Task task = manager.getTaskByTaskId(2);
        task.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task);

        assertEquals(TaskStatus.IN_PROGRESS, manager.getTaskByTaskId(1).getStatus());

        task = manager.getTaskByTaskId(2);
        task.setStatus(TaskStatus.DONE);
        manager.updateTask(task);

        assertEquals(TaskStatus.DONE, manager.getTaskByTaskId(1).getStatus());

        manager.createTask(new SubTask("e", "f", 1));

        assertEquals(TaskStatus.IN_PROGRESS, manager.getTaskByTaskId(1).getStatus());

        task = manager.getTaskByTaskId(2);
        task.setStatus(TaskStatus.NEW);
        manager.updateTask(task);

        assertEquals(TaskStatus.NEW, manager.getTaskByTaskId(1).getStatus());

        task = manager.getTaskByTaskId(2);
        task.setStatus(TaskStatus.DONE);
        manager.updateTask(task);
        task = manager.getTaskByTaskId(3);
        task.setStatus(TaskStatus.DONE);
        manager.updateTask(task);

        assertEquals(TaskStatus.DONE, manager.getTaskByTaskId(1).getStatus());
    }

    @Test
    public void haveSameTimeProperties() {
        InMemoryTaskManager inMemoryManager = (InMemoryTaskManager) manager;
        EpicTask epicTask1 = new EpicTask("a", "b");
        EpicTask epicTask2 = new EpicTask("c", "d");
        assertTrue(inMemoryManager.haveSameTimeProperties(epicTask1, epicTask2));

        epicTask1.setTimeProperties(startTime, 10);
        assertFalse(inMemoryManager.haveSameTimeProperties(epicTask1, epicTask2));

        epicTask1.resetTimeProperties();
        epicTask2.setTimeProperties(startTime, 60);
        assertFalse(inMemoryManager.haveSameTimeProperties(epicTask1, epicTask2));

        epicTask1.setTimeProperties(startTime, 10);
        assertFalse(inMemoryManager.haveSameTimeProperties(epicTask1, epicTask2));

        epicTask1.setTimeProperties(startTime.minusMinutes(-60), 60);
        assertFalse(inMemoryManager.haveSameTimeProperties(epicTask1, epicTask2));

        epicTask1.setTimeProperties(startTime, 60);
        assertTrue(inMemoryManager.haveSameTimeProperties(epicTask1, epicTask2));
    }

    @Test
    public void updateEpicTaskTimeProperties() {
        manager.createTask(new EpicTask("a", "b"));
        assertFalse(manager.getTaskByTaskId(1).areTimePropertiesSet());

        SubTask subTask1 = new SubTask("c", "d", 1);
        manager.createTask(subTask1);
        assertFalse(manager.getTaskByTaskId(1).areTimePropertiesSet());

        subTask1 = (SubTask) manager.getTaskByTaskId(2);
        subTask1.setTimeProperties(startTime, 60);
        manager.updateTask(subTask1);
        assertTrue(manager.getTaskByTaskId(1).areTimePropertiesSet());
        assertTrue(startTime.isEqual(manager.getTaskByTaskId(1).getStartTime()));
        assertEquals(60, manager.getTaskByTaskId(1).getDuration());
        assertTrue(startTime.plusMinutes(60).isEqual(manager.getTaskByTaskId(1).getEndTime()));

        SubTask subTask2 = new SubTask("e", "f", 1);
        manager.createTask(subTask2);
        assertFalse(manager.getTaskByTaskId(1).areTimePropertiesSet());

        subTask2 = (SubTask) manager.getTaskByTaskId(3);
        subTask2.setTimeProperties(startTime.plusMinutes(120), 60);
        manager.updateTask(subTask2);
        assertTrue(manager.getTaskByTaskId(1).areTimePropertiesSet());
        assertTrue(startTime.isEqual(manager.getTaskByTaskId(1).getStartTime()));
        assertEquals(180, manager.getTaskByTaskId(1).getDuration());
        assertTrue(startTime.plusMinutes(180).isEqual(manager.getTaskByTaskId(1).getEndTime()));

        manager.deleteTaskByTaskId(2);
        manager.deleteTaskByTaskId(3);
        assertFalse(manager.getTaskByTaskId(1).areTimePropertiesSet());
    }
    
    @Test
    public void addTaskToAndRemoveTaskFromTimetable() {
        Task task1 = new Task("a", "b");
        task1.setTimeProperties(startTime, 10);
        manager.createTask(task1);
        EpicTask epicTask = new EpicTask("c", "d");
        manager.createTask(epicTask);
        SubTask subTask1 = new SubTask("e", "f", 2);
        subTask1.setTimeProperties(startTime.plusMinutes(10), 10);
        manager.createTask(subTask1);
        SubTask subTask2 = new SubTask("g", "h", 2);
        subTask2.setTimeProperties(startTime.plusMinutes(20), 20);
        manager.createTask(subTask2);
        
        Task task2 = new Task("i", "j");
        task2.setTimeProperties(startTime.plusMinutes(30), 10);
        assertThrows(TaskTimeException.class, () -> manager.createTask(task2));
        SubTask subTask3 = new SubTask("k", "l", 2);
        subTask3.setTaskId(4);
        subTask3.setTimeProperties(startTime.plusMinutes(20), 10);
        manager.updateTask(subTask3);
        manager.createTask(task2);

        Task task3 = new Task("m", "n");
        task3.setTimeProperties(startTime.plusMinutes(35), 5);
        assertThrows(TaskTimeException.class, () -> manager.createTask(task3));
        manager.deleteTaskByTaskId(5);
        manager.createTask(task3);

        Task task4 = new Task("o", "p");
        task4.setTimeProperties(startTime.plusMinutes(25), 5);
        assertThrows(TaskTimeException.class, () -> manager.createTask(task4));
        manager.deleteTaskByTaskId(4);
        manager.createTask(task4);

        Task task5 = new Task("q", "r");
        task5.setTimeProperties(startTime.plusMinutes(15), 5);
        assertThrows(TaskTimeException.class, () -> manager.createTask(task5));
        manager.deleteTaskByTaskId(2);
        manager.createTask(task5);

        Task task6 = new Task("s", "t");
        task6.setTimeProperties(startTime.plusMinutes(5), 5);
        assertThrows(TaskTimeException.class, () -> manager.createTask(task6));
        manager.deleteAllTasks();
        manager.createTask(task6);
    }
}