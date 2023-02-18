package kanban.exceptions;

public class TaskTypeMismatchException extends TaskManagerException {
    public TaskTypeMismatchException(String message) {
        super(message);
    }
}