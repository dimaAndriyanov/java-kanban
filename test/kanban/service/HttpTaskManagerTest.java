package kanban.service;

import kanban.model.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {
    public KVServer kvServer;

    @BeforeEach
    public void setManager() throws IOException, InterruptedException {
        kvServer = new KVServer();
        kvServer.start();
        setManager(Managers.getHttpTaskManager(new URL("http://localhost:8078")));
    }

    @AfterEach
    public void stopKVServer() {
        kvServer.stop();
    }

    @Test
    public void saveToServerAndLoadFromServer() throws IOException, InterruptedException {
        assertNotNull(manager.getAllTasks());
        assertNotNull(manager.getHistory());
        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getHistory().isEmpty());

        createThreeDifferentTasks();
        Task task = new Task("g", "h");
        task.setTimeProperties(startTime, 60);
        manager.createTask(task);
        EpicTask epicTask = new EpicTask("i", "j");
        manager.createTask(epicTask);
        SubTask subTask = new SubTask("k", "l", 5);
        subTask.setTimeProperties(startTime.plusMinutes(120), 60);
        manager.createTask(subTask);

        HttpTaskManager newManager = Managers.getHttpTaskManager(new URL("http://localhost:8078"));
        assertNotNull(newManager.getHistory());
        assertTrue(newManager.getHistory().isEmpty());
        assertTaskListEquals(manager.getAllTasks(), newManager.getAllTasks());

        manager.getTaskByTaskId(4);
        manager.getTaskByTaskId(3);
        manager.getTaskByTaskId(1);
        manager.getAllSubTasksByEpicTaskId(2);

        newManager = Managers.getHttpTaskManager(new URL("http://localhost:8078"));
        assertTaskListEquals(manager.getHistory(), newManager.getHistory());
        assertTaskListEquals(manager.getAllTasks(), newManager.getAllTasks());
    }
}