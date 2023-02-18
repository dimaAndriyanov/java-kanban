package kanban.model;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

public class EpicTask extends Task {
    private final Set<Integer> subTasksIds = new LinkedHashSet<>();

    public EpicTask(String name, String description) {
        super(name, description);
    }

    public List<Integer> getSubTasksIds() {
        return new ArrayList<>(subTasksIds);
    }

    public void addSubTaskId(int subTaskId) {
        subTasksIds.add(subTaskId);
    }

    public void removeSubTaskId(int subTaskId) {
        subTasksIds.remove(subTaskId);
    }

    public boolean hasSubTasks() {
        return !subTasksIds.isEmpty();
    }

    @Override
    public String toString() {
        String result = "EpicTask{"
                + "name='" + getName() +"', ";
        if (getDescription() == null) {
            result += "description=null, ";
        } else {
            result += "description.length=" + getDescription().length() + ", ";
        }
        result += "taskId=" + getTaskId() + ", status='" + getStatus() + "', ";
        if (subTasksIds.isEmpty()) {
            result += "subTasksIds.isEmpty}";
        } else {
            result += "subTasksIds=" + subTasksIds + "}";
        }
        return result;
    }
}