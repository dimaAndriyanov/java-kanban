package kanban.service;

import kanban.exceptions.*;
import kanban.model.*;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    public TaskManager manager;
    public ZonedDateTime startTime = ZonedDateTime.of(
            2023,
            3,
            4,
            15,
            0,
            0,
            0,
            ZoneId.of("UTC+03:00"));

    public void setManager(T manager) {
        this.manager = manager;
    }

    public void createThreeDifferentTasks() {
        manager.createTask(new Task("a", "b"));
        manager.createTask(new EpicTask("c", "d"));
        manager.createTask(new SubTask("e", "f", 2));
    }

    public void assertTaskListEquals(List<Task> firstList, List<Task> secondList) {
        assertEquals(firstList.size(), secondList.size());
        for (int i = 0; i < firstList.size(); i++) {
            assertEquals(firstList.get(i).toString(), secondList.get(i).toString());
        }
    }

    @Test
    public void getAllTasks() {
        assertNotNull(manager.getAllTasks());
        assertTrue(manager.getAllTasks().isEmpty());

        Task task = new Task("a", "b");
        manager.createTask(task);
        assertArrayEquals(new Task[] {task}, manager.getAllTasks().toArray());

        EpicTask epicTask = new EpicTask("c", "d");
        manager.createTask(epicTask);
        assertArrayEquals(new Task[] {task, epicTask}, manager.getAllTasks().toArray());

        SubTask subTask = new SubTask("e", "f", 2);
        manager.createTask(subTask);
        assertArrayEquals(new Task[] {task, epicTask, subTask}, manager.getAllTasks().toArray());

        Task task2 = new Task("g", "h");
        manager.createTask(task2);
        assertArrayEquals(new Task[] {task, task2, epicTask, subTask}, manager.getAllTasks().toArray());
    }

    @Test
    public void deleteAllTasks() {
        createThreeDifferentTasks();

        assertFalse(manager.getAllTasks().isEmpty());

        manager.deleteAllTasks();
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    public void getTaskByTaskId() {
        Task task = new Task("a", "b");
        EpicTask epicTask = new EpicTask("c", "d");
        SubTask subTask = new SubTask("e", "f", 2);

        manager.createTask(task);
        manager.createTask(epicTask);

        manager.createTask(subTask);

        TaskManagerException exception = assertThrows(TaskManagerException.class, () -> manager.getTaskByTaskId(4));
        assertEquals("There is no Task with such taskId", exception.getMessage());

        assertEquals(task, manager.getTaskByTaskId(1));
        assertEquals(epicTask, manager.getTaskByTaskId(2));
        assertEquals(subTask, manager.getTaskByTaskId(3));
    }

    @Test
    public void createTask() {
        createThreeDifferentTasks();

        assertNotNull(manager.getTaskByTaskId(1));
        assertNotNull(manager.getTaskByTaskId(2));
        assertNotNull(manager.getTaskByTaskId(3));

        TaskManagerException exception = assertThrows(TaskManagerException.class, () -> manager.createTask(null));
        assertEquals("Can not create null Task", exception.getMessage());

        EpicTask epicTask = new EpicTask("g", "h");
        epicTask.addSubTaskId(3);
        exception = assertThrows(TaskManagerException.class, () -> manager.createTask(epicTask));
        assertEquals("EpicTask mast not have SubTasks", exception.getMessage());

        epicTask.removeSubTaskId(3);
        epicTask.setTimeProperties(startTime, 10);
        exception = assertThrows(TaskManagerException.class, () -> manager.createTask(epicTask));
        assertEquals("EpicTask mast not have TimePropertiesSet", exception.getMessage());

        SubTask subTask = new SubTask("i", "j", 4);
        NoSuchTaskException noSuchTaskException = assertThrows(
                NoSuchTaskException.class, () -> manager.createTask(subTask)
        );
        assertEquals("There is no Task with such masterTaskId", noSuchTaskException.getMessage());

        SubTask subTask2 = new SubTask("k", "l", 1);
        TaskTypeMismatchException taskTypeMismatchException = assertThrows(
                TaskTypeMismatchException.class, () -> manager.createTask(subTask2)
        );
        assertEquals("Task with such masterTaskId is not an EpicTask", taskTypeMismatchException.getMessage());

        Task task1 = new Task("m", "n");
        task1.setTimeProperties(startTime.minusYears(2), 10);
        exception = assertThrows(TaskManagerException.class, () -> manager.createTask(task1));
        assertEquals("Can not create Task with startTime in the Past", exception.getMessage());

        Task task2 = new Task("o", "p");
        task2.setTimeProperties(startTime, 10);
        manager.createTask(task2);
        Task task3 = new Task("q", "r");
        task3.setTimeProperties(startTime.plusMinutes(5), 5);
        TaskTimeException taskTimeException = assertThrows(TaskTimeException.class, () -> manager.createTask(task3));
        assertEquals("Task intersects with other existing tasks", taskTimeException.getMessage());
}

    @Test
    public void updateTask() {
        createThreeDifferentTasks();

        TaskManagerException taskManagerException = assertThrows(
                TaskManagerException.class, () -> manager.updateTask(null)
        );
        assertEquals("Can not update null Task", taskManagerException.getMessage());

        Task task = new Task("g", "h");
        task.setTaskId(4);
        NoSuchTaskException noSuchTaskException = assertThrows(
                NoSuchTaskException.class, () -> manager.updateTask(task)
        );
        assertEquals("There is no such task", noSuchTaskException.getMessage());

        EpicTask epicTask = new EpicTask("i", "j");
        epicTask.setTaskId(1);
        TaskTypeMismatchException taskTypeMismatchException = assertThrows(
                TaskTypeMismatchException.class, () -> manager.updateTask(epicTask)
        );
        assertEquals("Updated task and original task have different types",
                taskTypeMismatchException.getMessage()
        );

        EpicTask epicTask2 = new EpicTask("k", "l");
        epicTask2.setTaskId(2);
        taskManagerException = assertThrows(TaskManagerException.class, () -> manager.updateTask(epicTask2));
        assertEquals("Updated task and original task have different number of subTaskIds",
                taskManagerException.getMessage()
        );

        EpicTask epicTask3 = new EpicTask("m", "n");
        epicTask3.setTaskId(2);
        epicTask3.addSubTaskId(4);
        taskManagerException = assertThrows(TaskManagerException.class, () -> manager.updateTask(epicTask3));
        assertEquals("Updated task and original task have different subTaskIds",
                taskManagerException.getMessage()
        );
        EpicTask epicTask4 = new EpicTask("o", "p");
        epicTask4.setTaskId(2);
        epicTask4.addSubTaskId(3);
        epicTask4.setTimeProperties(startTime, 10);
        taskManagerException = assertThrows(TaskManagerException.class, () -> manager.updateTask(epicTask4));
        assertEquals("Updated task and original task have different Time Properties",
                taskManagerException.getMessage());

        SubTask subTask = new SubTask("q", "r", 1);
        subTask.setTaskId(3);
        taskManagerException = assertThrows(TaskManagerException.class, () -> manager.updateTask(subTask));
        assertEquals("Updated task and original task have different masterTaskIds",
                taskManagerException.getMessage()
        );

        Task task1 = new Task("s", "t");
        task1.setTaskId(1);
        task1.setTimeProperties(startTime.minusYears(2), 10);
        taskManagerException = assertThrows(TaskManagerException.class, () -> manager.updateTask(task1));
        assertEquals("Can not update Task with startTime in the Past", taskManagerException.getMessage());

        Task task2 = new Task("u", "v");
        task2.setTimeProperties(startTime, 10);
        manager.createTask(task2);
        Task task3 = new Task("w", "x");
        task3.setTaskId(1);
        task3.setTimeProperties(startTime.minusMinutes(5), 10);
        TaskTimeException taskTimeException = assertThrows(TaskTimeException.class, () -> manager.updateTask(task3));
        assertEquals("Updated Task intersects with other existing tasks", taskTimeException.getMessage());

        Task updatedTask = manager.getTaskByTaskId(1);
        Task updatedEpicTask = manager.getTaskByTaskId(2);
        Task updatedSubTask = manager.getTaskByTaskId(3);

        updatedTask.setName("newTaskName");
        updatedEpicTask.setName("newEpicTaskName");
        updatedSubTask.setName("newSubTaskName");

        manager.updateTask(updatedTask);
        manager.updateTask(updatedEpicTask);
        manager.updateTask(updatedSubTask);

        assertNotNull(manager.getTaskByTaskId(1));
        assertNotNull(manager.getTaskByTaskId(2));
        assertNotNull(manager.getTaskByTaskId(3));

        assertEquals("newTaskName", manager.getTaskByTaskId(1).getName());
        assertEquals("newEpicTaskName", manager.getTaskByTaskId(2).getName());
        assertEquals("newSubTaskName", manager.getTaskByTaskId(3).getName());
    }

    @Test
    public void deleteTaskByTaskId(){
        createThreeDifferentTasks();
        manager.createTask(new SubTask("g", "h", 2));
        EpicTask epicTask = new EpicTask("i", "j");
        SubTask subTask = new SubTask("k", "l", 5);
        manager.createTask(epicTask);
        manager.createTask(subTask);
        manager.createTask(new SubTask("m", "n", 5));

        NoSuchTaskException exception = assertThrows(NoSuchTaskException.class, () -> manager.deleteTaskByTaskId(8));
        assertEquals("There is no Task with such taskId", exception.getMessage());

        manager.deleteTaskByTaskId(1);
        manager.deleteTaskByTaskId(2);
        manager.deleteTaskByTaskId(7);

        assertArrayEquals(new Task[] {epicTask, subTask}, manager.getAllTasks().toArray());

        assertArrayEquals(((EpicTask) manager.getTaskByTaskId(5)).getSubTasksIds().toArray(), new Integer[] {6});
    }

    @Test
    public void getAllSubTasksByEpicTaskId() {
        manager.createTask(new Task("a", "b"));
        manager.createTask(new EpicTask("c", "d"));

        NoSuchTaskException noSuchTaskException = assertThrows(NoSuchTaskException.class,
                () -> manager.getAllSubTasksByEpicTaskId(3)
        );
        assertEquals("There is no task with such taskId", noSuchTaskException.getMessage());

        TaskTypeMismatchException taskTypeMismatchException = assertThrows(TaskTypeMismatchException.class,
                () -> manager.getAllSubTasksByEpicTaskId(1)
        );
        assertEquals("Task with such taskId is not an EpicTask", taskTypeMismatchException.getMessage());

        assertNotNull(manager.getAllSubTasksByEpicTaskId(2));
        assertTrue(manager.getAllSubTasksByEpicTaskId(2).isEmpty());

        SubTask subTask = new SubTask("e", "f", 2);
        manager.createTask(subTask);

        assertArrayEquals(new Task[] {subTask}, manager.getAllSubTasksByEpicTaskId(2).toArray());

        SubTask subTask2 = new SubTask("g", "h", 2);
        manager.createTask(subTask2);

        assertArrayEquals(new Task[] {subTask, subTask2}, manager.getAllSubTasksByEpicTaskId(2).toArray());
    }

    @Test
    public void getHistory() {
        assertNotNull(manager.getHistory());
        assertTrue(manager.getHistory().isEmpty());

        Task task1 = new Task("a", "b");
        EpicTask eTask2 = new EpicTask("c", "d");
        SubTask sTask3 = new SubTask("e", "f", 2);
        SubTask sTask4 = new SubTask("g", "h", 2);
        Task task5 = new Task("i", "j");
        EpicTask eTask6 = new EpicTask("k", "l");
        SubTask sTask7 = new SubTask("m", "n", 6);
        SubTask sTask8 = new SubTask("o", "p", 2);
        SubTask sTask9 = new SubTask("q", "r", 6);
        EpicTask eTask10 = new EpicTask("s", "t");

        manager.createTask(task1);
        manager.createTask(eTask2);
        manager.createTask(sTask3);
        manager.createTask(sTask4);
        manager.createTask(task5);
        manager.createTask(eTask6);
        manager.createTask(sTask7);
        manager.createTask(sTask8);
        manager.createTask(sTask9);
        manager.createTask(eTask10);

        assertTrue(manager.getHistory().isEmpty());

        eTask10.setName("correctedTask10");
        manager.updateTask(eTask10);

        assertTrue(manager.getHistory().isEmpty());

        manager.getTaskByTaskId(5);
        manager.getTaskByTaskId(2);
        manager.getTaskByTaskId(4);

        assertArrayEquals(new Task[] {task5, eTask2, sTask4}, manager.getHistory().toArray());

        manager.getTaskByTaskId(5);

        assertArrayEquals(new Task[] {eTask2, sTask4, task5}, manager.getHistory().toArray());

        manager.getAllSubTasksByEpicTaskId(2);

        assertArrayEquals(new Task[] {eTask2, task5, sTask3, sTask4, sTask8}, manager.getHistory().toArray());

        manager.getAllTasks();

        assertArrayEquals(new Task[] {task1, task5, eTask2, sTask3, sTask4, sTask8, eTask6, sTask7, sTask9, eTask10},
                manager.getHistory().toArray());

        manager.deleteTaskByTaskId(1);
        manager.deleteTaskByTaskId(2);
        manager.deleteTaskByTaskId(9);

        assertArrayEquals(new Task[] {task5, eTask6, sTask7, eTask10}, manager.getHistory().toArray());

        manager.deleteAllTasks();

        assertNotNull(manager.getHistory());
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    public void getPrioritizedTasks() {
        Task task1 = new Task("a", "b");
        task1.setTimeProperties(startTime, 10);
        manager.createTask(task1);
        Task task2 = new Task("c", "d");
        manager.createTask(task2);
        Task task3 = new Task("e", "f");
        task3.setTimeProperties(startTime.minusMinutes(60), 10);
        manager.createTask(task3);
        EpicTask epicTask1 = new EpicTask("g", "h");
        manager.createTask(epicTask1);
        assertArrayEquals(new Task[] {task3, task1, task2, epicTask1},
                manager.getPrioritizedTasks().toArray());

        SubTask subTask1 = new SubTask("i", "j", 4);
        subTask1.setTimeProperties(startTime.plusMinutes(30), 10);
        manager.createTask(subTask1);
        SubTask subTask2 = new SubTask("k", "l", 4);
        subTask2.setTimeProperties(startTime.minusMinutes(20), 10);
        manager.createTask(subTask2);
        SubTask subTask3 = new SubTask("m", "n", 4);
        subTask3.setTimeProperties(startTime.plusMinutes(90), 10);
        manager.createTask(subTask3);
        task1 = new Task("o", "p");
        task1.setTaskId(1);
        manager.updateTask(task1);
        task2 = new Task("q", "r");
        task2.setTaskId(2);
        task2.setTimeProperties(startTime.plusMinutes(10), 10);
        manager.updateTask(task2);
        assertArrayEquals(new Task[] {task3, subTask2, task2, subTask1, subTask3, task1, epicTask1},
                manager.getPrioritizedTasks().toArray());

        manager.deleteTaskByTaskId(3);
        manager.deleteTaskByTaskId(7);
        assertArrayEquals(new Task[] {subTask2, task2, subTask1, task1, epicTask1},
                manager.getPrioritizedTasks().toArray());

        manager.deleteTaskByTaskId(4);
        assertArrayEquals(new Task[] {task2, task1},
                manager.getPrioritizedTasks().toArray());

        manager.deleteAllTasks();
        assertTrue(manager.getPrioritizedTasks().isEmpty());
    }
}