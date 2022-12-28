package kanban.model;

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
        result += "taskId=" + getTaskId() + ", status='" + getStatus() + "', ";
        if (masterTaskId == 0) {
            result += "masterTask=null}";
        } else {
            result += "masterTaskId=" + masterTaskId + "}";
        }
        return result;
    }
}