package kanban.model;

import kanban.exceptions.TaskException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private int taskId;
    private TaskStatus status;
    private ZonedDateTime startTime;
    private int duration;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        taskId = 0;
        status = TaskStatus.NEW;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getTaskId() {
        return taskId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean areTimePropertiesSet() {
        return (startTime != null) && (duration > 0);
    }

    public void setTimeProperties(ZonedDateTime startTime, int duration) throws TaskException {
        if (startTime == null) {
            throw new TaskException("Task can not have null startTime");
        }
        if (duration == 0) {
            throw new TaskException("Task can not have 0 duration");
        }
        if (duration < 0) {
            throw new TaskException("Task can not have negative duration");
        }
        this.startTime = startTime;
        this.duration = duration;
    }

    public void resetTimeProperties() {
        startTime = null;
        duration = 0;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }

    public ZonedDateTime getEndTime() throws TaskException {
        if (!areTimePropertiesSet()) {
            throw new TaskException("Can not getEndTime from task with no TimeProperties");
        }
        return startTime.plusMinutes(duration);
    }

    public void changeZoneId(ZoneId zoneId) {
        if (zoneId == null) {
            throw new TaskException("Can not change ZoneId to null");
        }
        if (!areTimePropertiesSet()) {
            return;
        }
        startTime = startTime.withZoneSameInstant(zoneId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return taskId == task.taskId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }

    @Override
    public String toString() {
        String result = "Task{"
                + "name='" + name +"', ";
        if (description == null) {
            result += "description=null, ";
        } else {
            result += "description.length=" + description.length() + ", ";
        }
        result += "taskId=" + taskId + ", status='" + status + "', startTime=";
        if (startTime == null) {
            result += "null, duration=0}";
        } else {
            result += "'" +startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy.HH:mm.VV"))
                    + "', duration = " + duration +"}";
        }
        return result;
    }
}