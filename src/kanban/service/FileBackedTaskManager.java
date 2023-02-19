package kanban.service;

import kanban.exceptions.*;
import kanban.model.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    final private Path backupFile;

    public FileBackedTaskManager(int nextTaskId, HistoryManager historyManager, Path backupFile) {
        super(nextTaskId, historyManager);
        this.backupFile = backupFile;
    }

    @Override
    public List<Task> getAllTasks() throws FileBackedTaskManagerException {
        List<Task> result = super.getAllTasks();
        save();
        return result;
    }

    @Override
    public void deleteAllTasks() throws FileBackedTaskManagerException {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Task getTaskByTaskId(int taskId) throws TaskManagerException, FileBackedTaskManagerException {
        Task result = super.getTaskByTaskId(taskId);
        save();
        return result;
    }

    @Override
    public int createTask(Task task) throws TaskManagerException, FileBackedTaskManagerException {
        int result = super.createTask(task);
        save();
        return result;
    }

    @Override
    public int updateTask(Task task) throws TaskManagerException, FileBackedTaskManagerException {
        int result = super.updateTask(task);
        save();
        return result;
    }

    @Override
    public int deleteTaskByTaskId(int taskId) throws TaskManagerException, FileBackedTaskManagerException {
        int result = super.deleteTaskByTaskId(taskId);
        save();
        return result;
    }

    @Override
    public List<SubTask> getAllSubTasksByEpicTaskId(int taskId)
            throws TaskManagerException, FileBackedTaskManagerException {
        List <SubTask> result = super.getAllSubTasksByEpicTaskId(taskId);
        save();
        return result;
    }

    public static FileBackedTaskManager loadFromFile(Path file) throws ReadFromFileException {
        FileBackedTaskManager manager = new FileBackedTaskManager(1, new InMemoryHistoryManager(), file);
        try (BufferedReader fileReader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String lineFromFile = fileReader.readLine();
            if (lineFromFile == null || lineFromFile.isBlank()) {
                return manager;
            }
            int nextId = 1;
            while (!lineFromFile.isBlank()) {
                Task task = taskFromString(lineFromFile);
                manager.setNextTaskId(task.getTaskId());
                manager.createTaskFromParent(task);
                if (nextId < task.getTaskId() + 1) {
                    nextId = task.getTaskId() + 1;
                }
                lineFromFile = fileReader.readLine();
                if (lineFromFile == null) {
                    throw new ReadFromFileException();
                }
            }
            manager.setNextTaskId(nextId);
            lineFromFile = fileReader.readLine();
            if (lineFromFile == null) {
                throw new ReadFromFileException();
            }
            manager.addHistoryFromString(lineFromFile);
        } catch (IOException | InvalidDataException | TaskManagerException exception) {
            throw new ReadFromFileException();
        }
        return manager;
    }

    private int createTaskFromParent(Task task) throws TaskManagerException {
        return super.createTask(task);
    }

    private void save() throws SaveToFileException {
        try (BufferedWriter fileWriter = Files.newBufferedWriter(backupFile, StandardCharsets.UTF_8)) {
            for (Task task : getAllTasksNoHistory()) {
                fileWriter.write(toString(task));
                fileWriter.newLine();
            }
            fileWriter.newLine();
            fileWriter.write(historyToString());
        } catch (IOException exception) {
            throw new SaveToFileException();
        }
    }

    private static String toString(Task task) {
        StringBuilder result = new StringBuilder(task.getTaskId() + ",");
        if (task instanceof EpicTask) {
            result.append(TaskType.EPIC_TASK).append(",");
        } else if (task instanceof SubTask) {
            result.append(TaskType.SUBTASK).append(",");
        } else {
            result.append(TaskType.TASK).append(",");
        }
        result.append(String.join(",", task.getName(), task.getStatus().toString(), task.getDescription()));
        if (task instanceof SubTask) {
            result.append(",").append(((SubTask) task).getMasterTaskId());
        }
        return result.toString();
    }

    private static Task taskFromString(String value) throws InvalidDataException {
        Task task;
        String[] parts = value.split(",");
        try {
            switch (TaskType.valueOf(parts[1])) {
                case TASK:
                    task = new Task(parts[2], parts[4]);
                    break;
                case EPIC_TASK:
                    task = new EpicTask(parts[2], parts[4]);
                    break;
                case SUBTASK:
                    task = new SubTask(parts[2], parts[4], Integer.parseInt(parts[5]));
                    break;
                default:
                    throw new InvalidDataException();
            }
            task.setTaskId(Integer.parseInt(parts[0]));
            task.setStatus(TaskStatus.valueOf(parts[3]));
        } catch (Exception exception) {
            throw new InvalidDataException();
        }
        return task;
    }

    private String historyToString() {
        StringBuilder result = new StringBuilder();
        for (Task task : getHistory()) {
            result.append(task.getTaskId()).append(",");
        }
        if (result.length() > 0) {
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }

    private void addHistoryFromString(String value) throws InvalidDataException {
        String[] taskIds = value.split(",");
        try {
            for (String taskId : taskIds) {
                super.getTaskByTaskId(Integer.parseInt(taskId));
            }
        } catch (Exception exception) {
            throw new InvalidDataException();
        }
    }
}