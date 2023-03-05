package kanban.model;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

public class EpicTask extends Task {
    private final Set<Integer> subTasksIds = new LinkedHashSet<>();
    private ZonedDateTime endTime;

    public EpicTask(String name, String description) {
        super(name, description);
    }

    @Override
    public ZonedDateTime getEndTime() {
        return endTime;
    }

    @Override
    public void setTimeProperties(ZonedDateTime startTime, int duration) {
        super.setTimeProperties(startTime, duration);
        endTime = startTime.plusMinutes(duration);
    }

    @Override
    public void resetTimeProperties() {
        super.resetTimeProperties();
        endTime = null;
    }

    @Override
    public boolean areTimePropertiesSet() {
        return super.areTimePropertiesSet() && (endTime != null);
    }

    public List<Integer> getSubTasksIds() {
        return new ArrayList<>(subTasksIds);
    }

    public void addSubTaskId(int subTaskId) {
        subTasksIds.add(subTaskId);
    }

    public void removeSubTaskId(int subTaskId) {
        subTasksIds.remove(subTaskId);
    }

    public boolean hasSubTasks() {
        return !subTasksIds.isEmpty();
    }

    @Override
    public String toString() {
        String result = "EpicTask{"
                + "name='" + getName() +"', ";
        if (getDescription() == null) {
            result += "description=null, ";
        } else {
            result += "description.length=" + getDescription().length() + ", ";
        }
        result += "taskId=" + getTaskId() + ", status='" + getStatus() + "', startTime=";
        if (getStartTime() == null) {
            result += "null, duration=0, ";
        } else {
            result += "'" +getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy.HH:mm.VV"))
                    + "', duration = " + getDuration() +", ";
        }
        if (subTasksIds.isEmpty()) {
            result += "subTasksIds.isEmpty}";
        } else {
            result += "subTasksIds=" + subTasksIds + "}";
        }

        return result;
    }
}