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

    @Override
    public String toString() {
        String result = "Task{"
                + "name='" + name +"', ";
        if (description == null) {
            result += "description=null, ";
        } else {
            result += "description.length=" + description.length() + ", ";
        }
        result += "taskId=" + taskId + ", status='" + status + "'}";
        return result;
    }
}