package kanban.service;

import kanban.model.Node;
import kanban.model.Task;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager{
    private final Map<Integer, Node<Task>> watchedTasks = new HashMap<>();
    private Node<Task> head;
    private Node<Task> tail;

    @Override
    public List<Task> getHistory() {
        ArrayList<Task> history = new ArrayList<>();
        Node<Task> node = head;
        while (node != null) {
            history.add(node.getValue());
            node = node.getNext();
        }
        return history;
    }

    @Override
    public void add(Task task) {
        removeNode(watchedTasks.getOrDefault(task.getTaskId(), null));
        linkLast(task);
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

    private void linkLast(Task task) {
        Node<Task> newNode = new Node<>(task);
        if (tail == null) {
            head = newNode;
        } else {
            tail.setNext(newNode);
            newNode.setPrevious(tail);
        }
        tail = newNode;
        watchedTasks.put(task.getTaskId(), newNode);
    }

    private void removeNode (Node<Task> node) {
        if (node == null) {
            return;
        }
        if (node == head) {
            if (node == tail) {
                clear();
            } else {
                head = node.getNext();
                head.setPrevious(null);
            }
        } else if (node == tail) {
            tail = node.getPrevious();
            tail.setNext(null);
        } else {
            node.getPrevious().setNext(node.getNext());
            node.getNext().setPrevious(node.getPrevious());
        }
    }
}