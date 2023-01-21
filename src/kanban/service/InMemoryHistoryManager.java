package kanban.service;

import kanban.model.TaskIdNode;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager{
    private final Map<Integer, TaskIdNode> watchedTasks = new HashMap<>();
    private final TaskIdNode tail = new TaskIdNode(0);

    @Override
    public List<Integer> getHistory() {
        ArrayList<Integer> history = new ArrayList<>();
        TaskIdNode taskIdNode = tail.getPrevious();
        while (taskIdNode != null) {
            history.add(taskIdNode.getTaskId());
            taskIdNode = taskIdNode.getPrevious();
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
        tail.setPrevious(null);
    }

    private void linkLast(int taskId) {
        TaskIdNode newNode = new TaskIdNode(taskId);
        TaskIdNode lastNode = tail.getPrevious();
        if (lastNode != null) {
            lastNode.setNext(newNode);
            newNode.setPrevious(lastNode);
        }
        newNode.setNext(tail);
        tail.setPrevious(newNode);
        watchedTasks.put(taskId, newNode);
    }
    private void removeNode (TaskIdNode taskIdNode) {
        if (taskIdNode == null) {
            return;
        }
        if (taskIdNode == tail) {
            return;
        }
        TaskIdNode prevTask = taskIdNode.getPrevious();
        TaskIdNode nextTask = taskIdNode.getNext();
        if (prevTask != null) {
            prevTask.setNext(nextTask);
        }
        nextTask.setPrevious(prevTask);
    }
}