package kanban.service;

import kanban.model.*;
import kanban.exceptions.*;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int nextTaskId;
    private final HistoryManager historyManager;
    private final Map<Integer, Task> tasks = new HashMap<>();

    private final SortedSet<Task> sortedByStartTimeTasks = new TreeSet<>((task1, task2) -> {
            if (task1.getStartTime().isBefore(task2.getStartTime())) {
                return -1;
            } else if (task2.getStartTime().isBefore(task1.getStartTime())) {
                return 1;
            } else {
                return 0;
            }
        });
    private final Set<Task> tasksWithNoTimeProperties = new HashSet<>();
    private final Set<EpicTask> epicTasks = new HashSet<>();
    private final static ZonedDateTime START_TIME = ZonedDateTime.of(
            2023,
            1,
            1,
            0,
            0,
            0,
            0,
            ZoneId.of("UTC")
    );
    private ZonedDateTime endTime = START_TIME.plusYears(1);
    private final static int TIME_INTERVAL_LENGTH = 15;
    private final Map<TimeInterval, List<OccupiedTimeInterval<Integer>>> timetable = new HashMap<>();

    public InMemoryTaskManager(int nextTaskId, HistoryManager historyManager) {
        this.nextTaskId = nextTaskId;
        this.historyManager = historyManager;
        initiateTimetable();
    }

    @Override
    public List<Task> getAllTasks() {
        List<Task> result = getAllTasksNoHistory();
        for (Task task : result) {
            historyManager.add(task);
        }
        return result;
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
        historyManager.clear();
        sortedByStartTimeTasks.clear();
        tasksWithNoTimeProperties.clear();
        epicTasks.clear();
        timetable.clear();
        endTime = START_TIME.plusYears(1);
        initiateTimetable();
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
        if (task.areTimePropertiesSet() && task.getStartTime().isBefore(START_TIME)) {
            throw new TaskManagerException("Can not create Task with startTime in the Past");
        }
        if (!(task instanceof EpicTask)) {
            if (task.areTimePropertiesSet() && !isTaskTimeValid(task)) {
                throw new TaskTimeException("Task intersects with other existing tasks");
            }
        }
        if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            int masterTaskId = subTask.getMasterTaskId();
            if (!tasks.containsKey(masterTaskId)) {
                throw new NoSuchTaskException("There is no Task with such masterTaskId");
            }
            if (!(tasks.get(masterTaskId) instanceof EpicTask)) {
                throw new TaskTypeMismatchException("Task with such masterTaskId is not an EpicTask");
            }
            subTask.setTaskId(getNextTaskId());
            subTask.changeZoneId(ZoneId.of("UTC"));
            tasks.put(subTask.getTaskId(), subTask);
            addTaskToTimetable(subTask);
            addToPrioritizedSet(subTask);
            EpicTask masterTask = (EpicTask) tasks.get(masterTaskId);
            masterTask.addSubTaskId(subTask.getTaskId());
            updateEpicTaskStatus(masterTask);
            updateEpicTaskTimeProperties(masterTask);
            return subTask.getTaskId();
        }
        if (task instanceof EpicTask) {
            if (((EpicTask) task).hasSubTasks()) {
                throw new TaskManagerException("EpicTask mast not have SubTasks");
            }
            if (task.areTimePropertiesSet()) {
                throw new TaskManagerException("EpicTask mast not have TimePropertiesSet");
            }
        }
        task.setTaskId(getNextTaskId());
        task.changeZoneId(ZoneId.of("UTC"));
        tasks.put(task.getTaskId(), task);
        addTaskToTimetable(task);
        addToPrioritizedSet(task);
        return task.getTaskId();
    }

    @Override
    public int updateTask(Task task) throws TaskManagerException {
        if (task == null) {
            throw new TaskManagerException("Can not update null Task");
        }
        if (task.areTimePropertiesSet() && task.getStartTime().isBefore(START_TIME)) {
            throw new TaskManagerException("Can not update Task with startTime in the Past");
        }
        if (!(task instanceof EpicTask)) {
            if (task.areTimePropertiesSet() && !isTaskTimeValid(task)) {
                throw new TaskTimeException("Updated Task intersects with other existing tasks");
            }
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
            subTask.changeZoneId(ZoneId.of("UTC"));
            removeFromPrioritizedSet(tasks.get(subTask.getTaskId()));
            removeTaskFromTimetable(tasks.get(subTask.getTaskId()));
            tasks.put(subTask.getTaskId(), subTask);
            addToPrioritizedSet(subTask);
            addTaskToTimetable(subTask);
            updateEpicTaskStatus((EpicTask) tasks.get(subTask.getMasterTaskId()));
            updateEpicTaskTimeProperties((EpicTask) tasks.get(subTask.getMasterTaskId()));
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
            if (!haveSameTimeProperties(updatedEpicTask, originalEpicTask)) {
                throw new TaskManagerException("Updated task and original task have different Time Properties");
            }
        }
        task.changeZoneId(ZoneId.of("UTC"));
        removeFromPrioritizedSet(tasks.get(task.getTaskId()));
        removeTaskFromTimetable(tasks.get(task.getTaskId()));
        tasks.put(task.getTaskId(), task);
        addToPrioritizedSet(task);
        addTaskToTimetable(task);
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
            removeFromPrioritizedSet(subTask);
            removeTaskFromTimetable(subTask);
            EpicTask masterTask = (EpicTask) tasks.get(subTask.getMasterTaskId());
            masterTask.removeSubTaskId(taskId);
            updateEpicTaskStatus(masterTask);
            updateEpicTaskTimeProperties(masterTask);
            return taskId;
        }
        if (task instanceof EpicTask) {
            EpicTask epicTask = (EpicTask) task;
            for (Integer subTaskId : epicTask.getSubTasksIds()) {
                removeFromPrioritizedSet(tasks.get(subTaskId));
                removeTaskFromTimetable(tasks.get(subTaskId));
                tasks.remove(subTaskId);
                historyManager.remove(subTaskId);
            }
        }
        removeFromPrioritizedSet(task);
        removeTaskFromTimetable(task);
        tasks.remove(taskId);
        historyManager.remove(taskId);
        return taskId;
    }

    @Override
    public List<SubTask> getAllSubTasksByEpicTaskId(int taskId) throws TaskManagerException {
        List<SubTask> result = getAllSubTasksByEpicTaskIdNoHistory(taskId);
        for (Task task : result) {
            historyManager.add(task);
        }
        return result;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        List<Task> result = new ArrayList<>(sortedByStartTimeTasks);
        result.addAll(tasksWithNoTimeProperties);
        result.addAll(epicTasks);
        for (Task task : result) {
            historyManager.add(task);
        }
        return result;
    }

    void setNextTaskId(int nextTaskId) {
        this.nextTaskId = nextTaskId;
    }

    List<Task> getAllTasksNoHistory() {
        ArrayList<Task> allTasks = new ArrayList<>();
        ArrayList<Task> allEpicTasks = new ArrayList<>();
        for (Integer taskId : tasks.keySet()) {
            Task task = tasks.get(taskId);
            if (task instanceof SubTask) {
                continue;
            }
            if (task instanceof EpicTask) {
                allEpicTasks.add(task);
                allEpicTasks.addAll(getAllSubTasksByEpicTaskIdNoHistory(taskId));
                continue;
            }
            allTasks.add(task);
        }
        allTasks.addAll(allEpicTasks);
        return allTasks;
    }

    private List<SubTask> getAllSubTasksByEpicTaskIdNoHistory(int taskId) throws TaskManagerException {
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
        }
        return allSubTasks;
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

    boolean haveSameTimeProperties(EpicTask epicTask, EpicTask otherEpicTask) {
        if ((!epicTask.areTimePropertiesSet()) && (!otherEpicTask.areTimePropertiesSet())) {
            return true;
        } else if ((epicTask.areTimePropertiesSet()) && (otherEpicTask.areTimePropertiesSet())) {
            return epicTask.getStartTime().isEqual(otherEpicTask.getStartTime())
                    && (epicTask.getDuration() == otherEpicTask.getDuration())
                    && epicTask.getEndTime().isEqual(otherEpicTask.getEndTime());
        } else {
            return false;
        }
    }

    private void updateEpicTaskTimeProperties(EpicTask epicTask) {
        if (epicTask.getSubTasksIds().isEmpty()) {
            epicTask.resetTimeProperties();
            return;
        }
        Task subTask = tasks.get(epicTask.getSubTasksIds().get(0));
        if (!subTask.areTimePropertiesSet()) {
            epicTask.resetTimeProperties();
            return;
        }
        ZonedDateTime epicStartTime = subTask.getStartTime();
        ZonedDateTime epicEndTime = subTask.getEndTime();
        for (int i = 1; i < epicTask.getSubTasksIds().size(); i++) {
            subTask = tasks.get(epicTask.getSubTasksIds().get(i));
            if (!subTask.areTimePropertiesSet()) {
                epicTask.resetTimeProperties();
                return;
            }
            if (epicStartTime.isAfter(subTask.getStartTime())) {
                epicStartTime = subTask.getStartTime();
            }
            if (epicEndTime.isBefore(subTask.getEndTime())) {
                epicEndTime = subTask.getEndTime();
            }
        }
        epicTask.setTimeProperties(epicStartTime, (int) Duration.between(epicStartTime, epicEndTime).toMinutes());
    }

    private void addToPrioritizedSet(Task task) {
        if (task instanceof EpicTask) {
            epicTasks.add((EpicTask) task);
            return;
        }
        if (task.areTimePropertiesSet()) {
            sortedByStartTimeTasks.add(task);
        } else {
            tasksWithNoTimeProperties.add(task);
        }
    }

    private void removeFromPrioritizedSet(Task task) {
        if (task instanceof EpicTask) {
            epicTasks.remove((EpicTask) task);
            return;
        }
        if (task.areTimePropertiesSet()) {
            sortedByStartTimeTasks.remove(task);
        } else {
            tasksWithNoTimeProperties.remove(task);
        }
    }

    private void initiateTimetable() {
        ZonedDateTime from = START_TIME;
        ZonedDateTime to = START_TIME.plusMinutes(TIME_INTERVAL_LENGTH);
        while (to.isBefore(endTime)) {
            timetable.put(new TimeInterval(from, to), new ArrayList<>());
            from = to;
            to = to.plusMinutes(TIME_INTERVAL_LENGTH);
        }
        timetable.put(new TimeInterval(from, to), new ArrayList<>());
        endTime = to;
    }

    private void extendTimetable(ZonedDateTime newEndTime) {
        ZonedDateTime from = endTime;
        ZonedDateTime to = from.plusMinutes(TIME_INTERVAL_LENGTH);
        while (to.isBefore(newEndTime)) {
            timetable.put(new TimeInterval(from, to), new ArrayList<>());
            from = to;
            to = to.plusMinutes(TIME_INTERVAL_LENGTH);
        }
        timetable.put(new TimeInterval(from, to), new ArrayList<>());
        endTime = to;
    }

    private List<TimeInterval> getTimeIntervalsToCheck(Task task) {
        Duration beforeTaskStart = Duration.between(START_TIME, task.getStartTime());
        int numberOfTimeIntervalsBeforeTaskStart = (int) beforeTaskStart.toMinutes() / TIME_INTERVAL_LENGTH;
        List<TimeInterval> timeIntervalsToCheck = new ArrayList<>();
        if (task.getStartTime().isBefore(endTime)) {
            ZonedDateTime from = START_TIME.plusMinutes(numberOfTimeIntervalsBeforeTaskStart * TIME_INTERVAL_LENGTH);
            ZonedDateTime to = from.plusMinutes(TIME_INTERVAL_LENGTH);
            while (to.isBefore(task.getEndTime()) && to.isBefore(endTime)) {
                timeIntervalsToCheck.add(new TimeInterval(from, to));
                from = to;
                to = to.plusMinutes(15);
            }
            timeIntervalsToCheck.add(new TimeInterval(from, to));
        }
        return timeIntervalsToCheck;
    }
    private boolean isTaskTimeValid(Task task) {
        TimeInterval taskTimeInterval = new TimeInterval(task.getStartTime(), task.getEndTime());
        for (TimeInterval interval : getTimeIntervalsToCheck(task)) {
            for (OccupiedTimeInterval<Integer> occupiedInterval : timetable.get(interval)) {
                if (TimeInterval.doIntersect(taskTimeInterval, occupiedInterval)
                    && occupiedInterval.getValue() != task.getTaskId()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void removeTaskFromTimetable(Task task) {
        if (!task.areTimePropertiesSet() || task instanceof EpicTask) {
            return;
        }
        TimeInterval taskTimeInterval = new TimeInterval(task.getStartTime(), task.getEndTime());
        for (TimeInterval interval : getTimeIntervalsToCheck(task)) {
            timetable.get(interval).remove(new OccupiedTimeInterval<> (
                    TimeInterval.getIntersection(interval, taskTimeInterval), task.getTaskId()));
        }
    }

    private void addTaskToTimetable(Task task) {
        if (!task.areTimePropertiesSet() || task instanceof EpicTask) {
            return;
        }
        if (endTime.isBefore(task.getEndTime())) {
            extendTimetable(task.getEndTime());
        }
        TimeInterval taskTimeInterval = new TimeInterval(task.getStartTime(), task.getEndTime());
        for (TimeInterval interval : getTimeIntervalsToCheck(task)) {
            timetable.get(interval).add(new OccupiedTimeInterval<> (
                    TimeInterval.getIntersection(interval, taskTimeInterval), task.getTaskId()));
        }
    }

    private int getNextTaskId() {
        return nextTaskId++;
    }
}