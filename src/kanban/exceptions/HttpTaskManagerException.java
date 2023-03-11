package kanban.exceptions;

public class HttpTaskManagerException extends FileBackedTaskManagerException {
    public HttpTaskManagerException(String message) {
        super(message);
    }
}