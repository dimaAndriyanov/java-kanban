package kanban.service;

import kanban.exceptions.*;
import kanban.model.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path backupFile;

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

    @Override
    public List<Task> getPrioritizedTasks() throws FileBackedTaskManagerException {
        List<Task> result = super.getPrioritizedTasks();
        save();
        return result;
    }

    public static FileBackedTaskManager loadFromFile(Path file)
            throws TaskManagerException, FileBackedTaskManagerException, InvalidDataException {
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
        } catch (IOException exception) {
            throw new ReadFromFileException();
        }
        return manager;
    }

    void createTaskFromParent(Task task) throws TaskManagerException {
        super.createTask(task);
    }

    void save() throws SaveToFileException {
        try (BufferedWriter fileWriter = Files.newBufferedWriter(backupFile, StandardCharsets.UTF_8)) {
            for (Task task : getAllTasksNoHistory()) {
                fileWriter.write(toString(task));
                fileWriter.newLine();
            }
            fileWriter.newLine();
            String history = historyToString();
            if (!history.isEmpty()) {
                fileWriter.write(historyToString());
            } else {
                fileWriter.newLine();
            }
        } catch (IOException exception) {
            throw new SaveToFileException();
        }
    }

    static String toString(Task task) {
        StringBuilder result = new StringBuilder(task.getTaskId() + ",");
        if (task instanceof EpicTask) {
            result.append(TaskType.EPIC_TASK).append(",");
        } else if (task instanceof SubTask) {
            result.append(TaskType.SUBTASK).append(",");
        } else {
            result.append(TaskType.TASK).append(",");
        }
        result.append(String.join(",", task.getName(), task.getStatus().toString(), task.getDescription()));
        if (!(task instanceof EpicTask)) {
            if (!task.areTimePropertiesSet()) {
                result.append(",null,0");
            } else {
                result.append(",")
                        .append(task.getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy.HH:mm.VV")))
                        .append(",")
                        .append(task.getDuration());
            }
        }
        if (task instanceof SubTask) {
            result.append(",").append(((SubTask) task).getMasterTaskId());
        }
        return result.toString();
    }

    static Task taskFromString(String value) throws InvalidDataException {
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
                    task = new SubTask(parts[2], parts[4], Integer.parseInt(parts[7]));
                    break;
                default:
                    throw new InvalidDataException("Could not read task type from String");
            }
            task.setTaskId(Integer.parseInt(parts[0]));
            task.setStatus(TaskStatus.valueOf(parts[3]));
            if (!(task instanceof EpicTask)) {
                if (!(parts[5].equals("null"))) {
                    task.setTimeProperties(
                            ZonedDateTime.parse(parts[5], DateTimeFormatter.ofPattern("dd.MM.yyyy.HH:mm.VV")),
                            Integer.parseInt(parts[6])
                    );
                }
            }
        } catch (Exception exception) {
            throw new InvalidDataException("Could not read task from String");
        }
        return task;
    }

    String historyToString() {
        StringBuilder result = new StringBuilder();
        for (Task task : getHistory()) {
            result.append(task.getTaskId()).append(",");
        }
        if (result.length() > 0) {
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }

    void addHistoryFromString(String value) throws InvalidDataException {
        if (value.isEmpty()) {
            return;
        }
        String[] taskIds = value.split(",");
        try {
            for (String taskId : taskIds) {
                super.getTaskByTaskId(Integer.parseInt(taskId));
            }
        } catch (Exception exception) {
            throw new InvalidDataException("Could not read history from String");
        }
    }
}