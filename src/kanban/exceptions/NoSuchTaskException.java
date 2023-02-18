package kanban.exceptions;

public class NoSuchTaskException extends TaskManagerException {
    public NoSuchTaskException(String message) {
        super(message);
    }
}