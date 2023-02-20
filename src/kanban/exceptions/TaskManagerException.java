package kanban.exceptions;

public class TaskManagerException extends RuntimeException {
    public TaskManagerException(String message) {
        super(message);
    }
}