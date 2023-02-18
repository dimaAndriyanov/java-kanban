package kanban.service;

import kanban.model.*;
import kanban.exceptions.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private int nextTaskId;
    private final HistoryManager historyManager;
    private final Map<Integer, Task> tasks = new HashMap<>();

    public InMemoryTaskManager(int nextTaskId, HistoryManager historyManager) {
        this.nextTaskId = nextTaskId;
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getAllTasks() {
        ArrayList<Task> allTasks = new ArrayList<>();
        ArrayList<Task> allEpicTasks = new ArrayList<>();
        for (Integer taskId : tasks.keySet()) {
            Task task = tasks.get(taskId);
            if (task instanceof SubTask) {
                continue;
            }
            if (task instanceof EpicTask) {
                allEpicTasks.add(task);
                historyManager.add(task);
                try {
                    allEpicTasks.addAll(getAllSubTasksByEpicTask(taskId));
                } catch (TaskManagerException ignored) {
                }
                continue;
            }
            allTasks.add(task);
            historyManager.add(task);
        }
        allTasks.addAll(allEpicTasks);
        return allTasks;
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
        historyManager.clear();
    }

    @Override
    public Task getTaskByTaskId(int taskId) throws TaskManagerException {
        if (!tasks.containsKey(taskId)) {
            throw new NoSuchTaskException("There is no Task with such taskId");
        }
        Task task = tasks.get(taskId);
        historyManager.add(task);
        return task;
    }

    @Override
    public int createTask(Task task) throws TaskManagerException {
        if (task == null) {
            throw new TaskManagerException("Can not create null Task");
        }
        if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            int masterTaskId = subTask.getMasterTaskId();
            if (!tasks.containsKey(masterTaskId)) {
                throw new NoSuchTaskException("There is no Task with such musterTaskId");
            }
            if (!(tasks.get(masterTaskId) instanceof EpicTask)) {
                throw new TaskTypeMismatchException("Task with such masterTaskId is not an EpicTask");
            }
            subTask.setTaskId(getNextTaskId());
            tasks.put(subTask.getTaskId(), subTask);
            EpicTask masterTask = (EpicTask) tasks.get(masterTaskId);
            masterTask.addSubTaskId(subTask.getTaskId());
            updateEpicTaskStatus(masterTask);
            return subTask.getTaskId();
        }
        if (task instanceof EpicTask) {
            if (((EpicTask) task).hasSubTasks()) {
                throw new TaskManagerException("EpicTask mast not have SubTasks");
            }
        }
        task.setTaskId(getNextTaskId());
        tasks.put(task.getTaskId(), task);
        return task.getTaskId();
    }

    @Override
    public int updateTask(Task task) throws TaskManagerException {
        if (task == null) {
            throw new TaskManagerException("Can not update null Task");
        }
        if (!tasks.containsKey(task.getTaskId())) {
            throw new NoSuchTaskException("There is no such task");
        }
        if (task.getClass() != tasks.get(task.getTaskId()).getClass()) {
            throw new TaskTypeMismatchException("Updated task and original task have different types");
        }
        if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            if (subTask.getMasterTaskId() != ((SubTask) tasks.get(subTask.getTaskId())).getMasterTaskId()) {
                throw new TaskManagerException("Updated task and original task have different masterTaskIds");
            }
            tasks.put(subTask.getTaskId(), subTask);
            updateEpicTaskStatus((EpicTask) tasks.get(subTask.getMasterTaskId()));
            return subTask.getTaskId();
        }
        if (task instanceof EpicTask) {
            EpicTask updatedEpicTask = (EpicTask) task;
            EpicTask originalEpicTask = (EpicTask) tasks.get(updatedEpicTask.getTaskId());
            if (updatedEpicTask.getSubTasksIds().size() != originalEpicTask.getSubTasksIds().size()) {
                throw new TaskManagerException("Updated task and original task have different number of subTaskIds");
            }
            for (Integer subTaskId : updatedEpicTask.getSubTasksIds()) {
                if (!originalEpicTask.getSubTasksIds().contains(subTaskId)) {
                    throw new TaskManagerException("Updated task and original task have different subTaskIds");
                }
            }
        }
        tasks.put(task.getTaskId(), task);
        return task.getTaskId();
    }

    @Override
    public int deleteTaskByTaskId(int taskId) throws TaskManagerException {
        if (!tasks.containsKey(taskId)) {
            throw new NoSuchTaskException("There is no Task with such taskId");
        }
        Task task = tasks.get(taskId);
        if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            tasks.remove(taskId);
            historyManager.remove(taskId);
            EpicTask masterTask = (EpicTask) tasks.get(subTask.getMasterTaskId());
            masterTask.removeSubTaskId(taskId);
            updateEpicTaskStatus(masterTask);
            return taskId;
        }
        if (task instanceof EpicTask) {
            EpicTask epicTask = (EpicTask) task;
            for (Integer subTaskId : epicTask.getSubTasksIds()) {
                tasks.remove(subTaskId);
                historyManager.remove(subTaskId);
            }
        }
        tasks.remove(taskId);
        historyManager.remove(taskId);
        return taskId;
    }

    @Override
    public List<SubTask> getAllSubTasksByEpicTask(int taskId) throws TaskManagerException {
        if (!tasks.containsKey(taskId)) {
            throw new NoSuchTaskException("There is no task with such taskId");
        }
        if (!(tasks.get(taskId) instanceof EpicTask)) {
            throw new TaskTypeMismatchException("Task with such taskId is not an EpicTask");
        }
        EpicTask epicTask = (EpicTask) tasks.get(taskId);
        ArrayList<SubTask> allSubTasks = new ArrayList<>();
        for (Integer subTaskId : epicTask.getSubTasksIds()) {
            allSubTasks.add((SubTask) tasks.get(subTaskId));
            historyManager.add(tasks.get(subTaskId));
        }
        return allSubTasks;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicTaskStatus(EpicTask epicTask) {
        if (epicTask.getSubTasksIds().isEmpty()) {
            epicTask.setStatus(TaskStatus.NEW);
            return;
        }
        TaskStatus status = tasks.get(epicTask.getSubTasksIds().get(0)).getStatus();
        if (status == TaskStatus.IN_PROGRESS) {
            epicTask.setStatus(TaskStatus.IN_PROGRESS);
            return;
        }
        for (int i = 1; i < epicTask.getSubTasksIds().size(); i++) {
            if (status != tasks.get(epicTask.getSubTasksIds().get(i)).getStatus()) {
                epicTask.setStatus(TaskStatus.IN_PROGRESS);
                return;
            }
        }
        epicTask.setStatus(status);
    }

    private int getNextTaskId() {
        return nextTaskId++;
    }
}