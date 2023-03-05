package kanban.model;

import java.time.format.DateTimeFormatter;

public class SubTask extends Task {
    private final int masterTaskId;

    public SubTask(String name, String description, int masterTaskId) {
        super(name, description);
        this.masterTaskId = masterTaskId;
    }

    public int getMasterTaskId() {
        return masterTaskId;
    }

    @Override
    public String toString() {
        String result = "SubTask{"
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
        if (masterTaskId == 0) {
            result += "masterTask=null}";
        } else {
            result += "masterTaskId=" + masterTaskId + "}";
        }

        return result;
    }
}