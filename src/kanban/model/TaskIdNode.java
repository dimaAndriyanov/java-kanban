package kanban.model;

public class TaskIdNode {
    private final int taskId;
    private TaskIdNode prev;
    private TaskIdNode next;

    public TaskIdNode(int taskId) {
        this.taskId = taskId;
        prev = null;
        next = null;
    }

    public int getTaskId() {
        return taskId;
    }

    public TaskIdNode getPrevious() {
        return prev;
    }

    public TaskIdNode getNext() {
        return next;
    }

    public void setPrevious(TaskIdNode taskIdNode) {
        prev = taskIdNode;
    }

    public void setNext(TaskIdNode taskIdNode) {
        next = taskIdNode;
    }
}