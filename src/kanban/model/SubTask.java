package kanban.model;

//Data-класс для хранения данных задачи, которая является подзадачей
public class SubTask extends Task {
    private int masterTaskId;

    public SubTask(String name, String description) {
        super(name, description);
    }

    public void setMasterTaskId(int masterTaskId) {
        this.masterTaskId = masterTaskId;
    }

    public int getMasterTaskId() {
        return this.masterTaskId;
    }

    @Override
    public String toString() {
        String result = "SubTask{"
                + "name='" + this.getName() +"', ";
        if (this.getDescription() == null) {
            result += "description=null, ";
        } else {
            result += "description.length=" + this.getDescription().length() + ", ";
        }
        result += "taskId=" + this.getTaskId() + ", status='" + this.getStatus() + "', ";
        if (this.masterTaskId == 0) {
            result += "masterTask=null}";
        } else {
            result += "masterTaskId=" + this.masterTaskId + "}";
        }
        return result;
    }
}