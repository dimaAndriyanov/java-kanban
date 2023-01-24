package kanban.service;

import kanban.model.TaskIdNode;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager{
    private final Map<Integer, TaskIdNode> watchedTasks = new HashMap<>();
    private TaskIdNode head;
    private TaskIdNode tail;

    @Override
    public List<Integer> getHistory() {
        ArrayList<Integer> history = new ArrayList<>();
        TaskIdNode taskIdNode = head;
        while (taskIdNode != null) {
            history.add(taskIdNode.getTaskId());
            taskIdNode = taskIdNode.getNext();
        }
        return history;
    }

    @Override
    public void add(int taskId) {
        removeNode(watchedTasks.getOrDefault(taskId, null));
        linkLast(taskId);
    }

    @Override
    public void remove(int taskId) {
        removeNode(watchedTasks.getOrDefault(taskId, null));
        watchedTasks.remove(taskId);
    }

    @Override
    public void clear() {
        head = null;
        tail = null;
    }

    private void linkLast(int taskId) {
        TaskIdNode newNode = new TaskIdNode(taskId);
        if (tail == null) {
            head = newNode;
        } else {
            tail.setNext(newNode);
            newNode.setPrevious(tail);
        }
        tail = newNode;
        watchedTasks.put(taskId, newNode);
    }
    private void removeNode (TaskIdNode taskIdNode) {
        if (taskIdNode == null) {
            return;
        }
        if (taskIdNode == head) {
            if (taskIdNode == tail) {
                clear();
            } else {
                head = taskIdNode.getNext();
                head.setPrevious(null);
            }
            return;
        }
        if (taskIdNode == tail) {
            tail = taskIdNode.getPrevious();
            tail.setNext(null);
            return;
        }
        taskIdNode.getPrevious().setNext(taskIdNode.getNext());
        taskIdNode.getNext().setPrevious(taskIdNode.getPrevious());
    }
}