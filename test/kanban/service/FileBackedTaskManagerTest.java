package kanban.service;

import kanban.exceptions.ReadFromFileException;
import kanban.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    @BeforeEach
    public void setManager() {
        setManager(new FileBackedTaskManager(1,
                new InMemoryHistoryManager(),
                Paths.get("resources", "BackupFileTest.csv"))
        );
    }

    public void assertTaskListEquals(List<Task> firstList, List<Task> secondList) {
        assertEquals(firstList.size(), secondList.size());
        for (int i = 0; i < firstList.size(); i++) {
            assertEquals(firstList.get(i).toString(), secondList.get(i).toString());
        }
    }

    @Test
    public void saveToFileAndLoadFromFileTest() {
        assertThrows(ReadFromFileException.class, () -> FileBackedTaskManager.
                loadFromFile(Paths.get("resources", "Back_Up_File")));

        FileBackedTaskManager newManager = FileBackedTaskManager.
                loadFromFile(Paths.get("resources", "EmptyBackupFileTest.csv"));
        assertNotNull(newManager.getAllTasks());
        assertNotNull(newManager.getHistory());
        assertTrue(newManager.getAllTasks().isEmpty());
        assertTrue(newManager.getHistory().isEmpty());

        createThreeDifferentTasks();
        Task task = new Task("g", "h");
        task.setTimeProperties(startTime, 60);
        manager.createTask(task);
        EpicTask epicTask = new EpicTask("i", "j");
        manager.createTask(epicTask);
        SubTask subTask = new SubTask("k", "l", 5);
        subTask.setTimeProperties(startTime.plusMinutes(120), 60);
        manager.createTask(subTask);

        Path backupFileTest = Paths.get("resources", "BackupFileTest.csv");
        newManager = FileBackedTaskManager.loadFromFile(backupFileTest);
        assertNotNull(newManager.getHistory());
        assertTrue(newManager.getHistory().isEmpty());
        assertTaskListEquals(manager.getAllTasks(), newManager.getAllTasks());

        manager.getTaskByTaskId(4);
        manager.getTaskByTaskId(3);
        manager.getTaskByTaskId(1);
        manager.getAllSubTasksByEpicTaskId(2);

        newManager = FileBackedTaskManager.loadFromFile(backupFileTest);
        assertTaskListEquals(manager.getHistory(), newManager.getHistory());
        assertTaskListEquals(manager.getAllTasks(), newManager.getAllTasks());
    }
}