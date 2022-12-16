package kanban.service;

import kanban.model.*;
import java.util.ArrayList;
import java.util.HashMap;

//Manager-класс для управления задачами
public class TaskManager {
    private int nextTaskId = 1; // Генератор уникальных id для задач
    private HashMap<Integer, Task> tasks = new HashMap<>(); // Список всех отслеживаемых задач

    // Возвращает список всех отслеживаемых задач. сначала в списке идут обычные задачи, потом сложные
    // после каждой сложной задачи идет список ее подзадач
    public ArrayList<Task> getAllTasks() {
        ArrayList<Task> allTasks = new ArrayList<>();
        if (this.tasks.isEmpty()) {
            return allTasks;
        }
        ArrayList<Task> allEpicTasks = new ArrayList<>();
        for (Integer taskId : this.tasks.keySet()) {
            Task task = this.tasks.get(taskId);
            if (task instanceof SubTask) {
                continue;
            }
            if (task instanceof EpicTask) {
                allEpicTasks.add(task);
                EpicTask epicTask = (EpicTask) task;
                for (Integer subTaskId : epicTask.getSubTasksIds()) {
                    if (this.tasks.containsKey(subTaskId)
                            && (this.tasks.get(subTaskId) instanceof SubTask)) {
                        allEpicTasks.add(this.tasks.get(subTaskId));
                    } else {
                        allEpicTasks.add(null);
                    }
                }
                continue;
            }
            allTasks.add(task);
        }
        allTasks.addAll(allEpicTasks);
        return allTasks;
    }

    public void deleteAllTasks() {
        this.tasks.clear();
    }

    public Task getTaskByTaskId(int taskId) {
        if (!this.tasks.containsKey(taskId)) {
            return null;
        }
        return this.tasks.get(taskId);
    }

    // Добавляет новую задачу в список отслеживаемых задач
    // Возвращает уникальный id задачи при ее добавлении, или 0 при неверном обращении (задача не добавляется в список).
    // При добавлении сложная задача не знает ничего о своих подзадачах.
    // При добавлении подзадача уже должна иметь ссылку на сложную задачу,
    // частью которой является. Сложная задача, при этом, должна находиться в
    // списке отслеживаемых задач
    public int createTask(Task task) {
        if (task == null) {
            return 0;
        }
        if (task instanceof EpicTask) {
            task.setTaskId(this.nextTaskId++);
            this.tasks.put(task.getTaskId(), task);
            return task.getTaskId();
        }
        if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            int masterTaskId = subTask.getMasterTaskId();
            if (!this.tasks.containsKey(masterTaskId)
                    || !(this.tasks.get(masterTaskId) instanceof EpicTask)) {
                return 0;
            }
            subTask.setTaskId(this.nextTaskId++);
            this.tasks.put(subTask.getTaskId(), subTask);
            EpicTask masterTask = (EpicTask) this.tasks.get(masterTaskId);
            masterTask.addSubTaskId(subTask.getTaskId());
            this.updateEpicTaskStatus(masterTask);
            return subTask.getTaskId();
        }
        task.setTaskId(this.nextTaskId++);
        this.tasks.put(task.getTaskId(), task);
        return task.getTaskId();
    }

    // Обнавляет задачу, находящуюся в списке отслеживаемых задач. Возвращает id задачи при успешном обновлении
    // и 0 при неверном обращении (обновление не происходит)
    // Объект task, передаваемый в качестве параметра метода заменяет объект, находящийся в списке отслеживаемых задач.
    // Объект task и обновляемая задача должны иметь:
    // - одинаковые id
    // - одинаковые типы
    // Подзадачи также должны иметь одинаковые сложные задачи, частью которых являются.
    // Сложные задачи также должны иметь одниковые подзадачи.
    public int updateTask(Task task) {
        if (task == null || !this.tasks.containsKey(task.getTaskId())
            || (task.getClass() != this.tasks.get(task.getTaskId()).getClass())) {
            return 0;
        }
        if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            if (subTask.getMasterTaskId() != ((SubTask) this.tasks.get(subTask.getTaskId())).getMasterTaskId()) {
                return 0;
            }
            if (!this.tasks.containsKey(subTask.getMasterTaskId())
                    || !(this.tasks.get(subTask.getMasterTaskId()) instanceof EpicTask)) {
                return 0;
            }
            this.tasks.put(subTask.getTaskId(), subTask);
            this.updateEpicTaskStatus((EpicTask) this.tasks.get(subTask.getMasterTaskId()));
            return subTask.getTaskId();
        }
        if (task instanceof EpicTask) {
            EpicTask updatedEpicTask = (EpicTask) task;
            EpicTask originalEpicTask = (EpicTask) this.tasks.get(updatedEpicTask.getTaskId());
            if (updatedEpicTask.getSubTasksIds().size() != originalEpicTask.getSubTasksIds().size()) {
                return 0;
            }
            for (Integer subTaskId : updatedEpicTask.getSubTasksIds()) {
                if (!originalEpicTask.getSubTasksIds().contains(subTaskId)) {
                    return 0;
                }
            }
            this.tasks.put(updatedEpicTask.getTaskId(), updatedEpicTask);
            return updatedEpicTask.getTaskId();
        }
        this.tasks.put(task.getTaskId(), task);
        return task.getTaskId();
    }

    // Удаляет задачу с id taskId из списка отслеживаемых задач и возвращает taskId.
    // При неверном обращении возвращает 0.
    // При удалении подзадачи происходит редактирование сложной задачи, частью которой она является.
    // При неверной ссылке на сложную задачу подзадача все равно удаляется, но метод возвращает 0.
    public int deleteTaskByTaskId(int taskId) {
        if (!this.tasks.containsKey(taskId)) {
            return 0;
        }
        Task task = this.tasks.get(taskId);
        if (task instanceof EpicTask) {
            EpicTask epicTask = (EpicTask) task;
            for (Integer subTaskId : epicTask.getSubTasksIds()) {
                if (this.tasks.containsKey(subTaskId) && (this.tasks.get(subTaskId) instanceof SubTask)) {
                    this.tasks.remove(subTaskId);
                }
            }
            this.tasks.remove(taskId);
            return taskId;
        }
        if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            if (!this.tasks.containsKey(subTask.getMasterTaskId())
                    || !(this.tasks.get(subTask.getMasterTaskId()) instanceof EpicTask)) {
                this.tasks.remove(taskId);
                return 0;
            }
            EpicTask masterTask = (EpicTask) this.tasks.get(subTask.getMasterTaskId());
            masterTask.removeSubTaskId(taskId);
            this.tasks.remove(taskId);
            this.updateEpicTaskStatus(masterTask);
            return taskId;
        }
        this.tasks.remove(taskId);
        return taskId;
    }

    // Возвращает список подзадач сложной задачи task
    // Есди подзадачи нет в списке отслеживаемых задач, или она не является подзадачей, то в возвращаемый список
    // подзадач добавляется mull.
    public ArrayList<SubTask> getAllSubTasksByEpicTask(Task task) {
        if (!(task instanceof EpicTask)) {
            return null;
        }
        ArrayList<SubTask> allSubTasks = new ArrayList<>();
        for (Integer subTaskId : ((EpicTask) task).getSubTasksIds()) {
            if (this.tasks.containsKey(subTaskId)
                && this.tasks.get(subTaskId) instanceof SubTask) {
                allSubTasks.add((SubTask) this.tasks.get(subTaskId));
            } else {
                allSubTasks.add(null);
            }
        }
        return allSubTasks;
    }

    // Устанавливает статус сложной задачи на основе ее подзадач
    // Метод вызывается при добавлении/обнавлении/удалении подзадачи
    private boolean updateEpicTaskStatus(EpicTask epicTask) {
        if (epicTask.getSubTasksIds().isEmpty()) {
            epicTask.setStatus(TaskStatus.NEW);
            return true;
        }
        for (Integer subTaskId : epicTask.getSubTasksIds()) {
            if (!this.tasks.containsKey(subTaskId)) {
                return false;
            }
        }
        TaskStatus status = this.tasks.get(epicTask.getSubTasksIds().get(0)).getStatus();
        if (status == TaskStatus.IN_PROGRESS) {
            epicTask.setStatus(TaskStatus.IN_PROGRESS);
            return true;
        }
        for (int i = 1; i < epicTask.getSubTasksIds().size(); i++) {
            if (status != this.tasks.get(epicTask.getSubTasksIds().get(i)).getStatus()) {
                epicTask.setStatus(TaskStatus.IN_PROGRESS);
                return true;
            }
        }
        epicTask.setStatus(status);
        return true;
    }
}