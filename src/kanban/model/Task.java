package kanban.model;

//Data-класс для хранения данных о задаче
public class Task {
    private String name;
    private String description;
    private int taskId;
    private TaskStatus status;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.taskId = 0;
        this.status = TaskStatus.NEW;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public int getTaskId() {
        return this.taskId;
    }

    public TaskStatus getStatus() {
        return this.status;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String result = "Task{"
                + "name='" + this.name +"', ";
        if (this.description == null) {
            result += "description=null, ";
        } else {
            result += "description.length=" + this.description.length() + ", ";
        }
        result += "taskId=" + this.taskId + ", status='" + status + "'}";
        return result;
    }
}