package kanban.model;

import java.util.ArrayList;

//Data-класс для хранения данных о сложной задаче (содержащей подзадачи)
public class EpicTask extends Task {
    private ArrayList<Integer> subTasksIds = new ArrayList<>();

    public EpicTask(String name, String description) {
        super(name, description);
    }

    public ArrayList<Integer> getSubTasksIds() {
        return subTasksIds;
    }

    public void addSubTaskId(int subTaskId) {
        subTasksIds.add(subTaskId);
    }

    public void removeSubTaskId(int subTaskId) {
        subTasksIds.remove((Integer) subTaskId);
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
            result += "subTasksIds=[" + subTasksIds.get(0);
            for (int i = 1; i < subTasksIds.size(); i++) {
                result += ", " + subTasksIds.get(i);
            }
            result +="]}";
        }
        return result;
    }
}