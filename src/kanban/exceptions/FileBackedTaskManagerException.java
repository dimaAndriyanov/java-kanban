package kanban.exceptions;

public class FileBackedTaskManagerException extends RuntimeException {
    public FileBackedTaskManagerException() {
        super();
    }

    public FileBackedTaskManagerException(String message) {
        super(message);
    }
}