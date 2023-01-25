package kanban.service;

import kanban.model.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager{
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
                allEpicTasks.addAll(getAllSubTasksByEpicTask(task));
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
    public Task getTaskByTaskId(int taskId) {
        if (!tasks.containsKey(taskId)) {
            return null;
        }
        Task task = tasks.get(taskId);
        historyManager.add(task);
        return task;
    }

    @Override
    public int createTask(Task task) {
        if (task == null) {
            return 0;
        }
        if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            int masterTaskId = subTask.getMasterTaskId();
            if (!tasks.containsKey(masterTaskId)
                    || !(tasks.get(masterTaskId) instanceof EpicTask)) {
                return 0;
            }
            subTask.setTaskId(getNextTaskId());
            tasks.put(subTask.getTaskId(), subTask);
            EpicTask masterTask = (EpicTask) tasks.get(masterTaskId);
            masterTask.addSubTaskId(subTask.getTaskId());
            updateEpicTaskStatus(masterTask);
            return subTask.getTaskId();
        }
        task.setTaskId(getNextTaskId());
        tasks.put(task.getTaskId(), task);
        return task.getTaskId();
    }

    @Override
    public int updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getTaskId())
            || (task.getClass() != tasks.get(task.getTaskId()).getClass())) {
            return 0;
        }
        if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            if (subTask.getMasterTaskId() != ((SubTask) tasks.get(subTask.getTaskId())).getMasterTaskId()) {
                return 0;
            }
            if (!tasks.containsKey(subTask.getMasterTaskId())
                    || !(tasks.get(subTask.getMasterTaskId()) instanceof EpicTask)) {
                return 0;
            }
            tasks.put(subTask.getTaskId(), subTask);
            updateEpicTaskStatus((EpicTask) tasks.get(subTask.getMasterTaskId()));
            return subTask.getTaskId();
        }
        if (task instanceof EpicTask) {
            EpicTask updatedEpicTask = (EpicTask) task;
            EpicTask originalEpicTask = (EpicTask) tasks.get(updatedEpicTask.getTaskId());
            if (updatedEpicTask.getSubTasksIds().size() != originalEpicTask.getSubTasksIds().size()) {
                return 0;
            }
            for (Integer subTaskId : updatedEpicTask.getSubTasksIds()) {
                if (!originalEpicTask.getSubTasksIds().contains(subTaskId)) {
                    return 0;
                }
            }
            tasks.put(updatedEpicTask.getTaskId(), updatedEpicTask);
            return updatedEpicTask.getTaskId();
        }
        tasks.put(task.getTaskId(), task);
        return task.getTaskId();
    }

    @Override
    public int deleteTaskByTaskId(int taskId) {
        if (!tasks.containsKey(taskId)) {
            return 0;
        }
        Task task = tasks.get(taskId);
        if (task instanceof EpicTask) {
            EpicTask epicTask = (EpicTask) task;
            for (Integer subTaskId : epicTask.getSubTasksIds()) {
                if (tasks.containsKey(subTaskId) && (tasks.get(subTaskId) instanceof SubTask)) {
                    tasks.remove(subTaskId);
                    historyManager.remove(subTaskId);
                }
            }
            tasks.remove(taskId);
            historyManager.remove(taskId);
            return taskId;
        }
        if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            if (!tasks.containsKey(subTask.getMasterTaskId())
                    || !(tasks.get(subTask.getMasterTaskId()) instanceof EpicTask)) {
                tasks.remove(taskId);
                historyManager.remove(taskId);
                return 0;
            }
            EpicTask masterTask = (EpicTask) tasks.get(subTask.getMasterTaskId());
            masterTask.removeSubTaskId(taskId);
            tasks.remove(taskId);
            historyManager.remove(taskId);
            updateEpicTaskStatus(masterTask);
            return taskId;
        }
        tasks.remove(taskId);
        historyManager.remove(taskId);
        return taskId;
    }

    @Override
    public List<SubTask> getAllSubTasksByEpicTask(Task task) {
        if (!(task instanceof EpicTask)) {
            return null;
        }
        ArrayList<SubTask> allSubTasks = new ArrayList<>();
        for (Integer subTaskId : ((EpicTask) task).getSubTasksIds()) {
            if (tasks.containsKey(subTaskId)
                && tasks.get(subTaskId) instanceof SubTask) {
                allSubTasks.add((SubTask) tasks.get(subTaskId));
                historyManager.add(tasks.get(subTaskId));
            } else {
                allSubTasks.add(null);
            }
        }
        return allSubTasks;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private boolean updateEpicTaskStatus(EpicTask epicTask) {
        if (epicTask.getSubTasksIds().isEmpty()) {
            epicTask.setStatus(TaskStatus.NEW);
            return true;
        }
        for (Integer subTaskId : epicTask.getSubTasksIds()) {
            if (!tasks.containsKey(subTaskId)) {
                return false;
            }
        }
        TaskStatus status = tasks.get(epicTask.getSubTasksIds().get(0)).getStatus();
        if (status == TaskStatus.IN_PROGRESS) {
            epicTask.setStatus(TaskStatus.IN_PROGRESS);
            return true;
        }
        for (int i = 1; i < epicTask.getSubTasksIds().size(); i++) {
            if (status != tasks.get(epicTask.getSubTasksIds().get(i)).getStatus()) {
                epicTask.setStatus(TaskStatus.IN_PROGRESS);
                return true;
            }
        }
        epicTask.setStatus(status);
        return true;
    }

    private int getNextTaskId() {
        return nextTaskId++;
    }
}