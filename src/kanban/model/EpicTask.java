package kanban.model;

import java.util.ArrayList;

//Data-класс для хранения данных о сложной задаче (содержащей подзадачи)
public class EpicTask extends Task {
    private ArrayList<Integer> subTasksIds = new ArrayList<>();

    public EpicTask(String name, String description) {
        super(name, description);
    }

    public ArrayList<Integer> getSubTasksIds() {
        return this.subTasksIds;
    }

    public void addSubTaskId(int subTaskId) {
        this.subTasksIds.add(subTaskId);
    }

    public void removeSubTaskId(int subTaskId) {
        this.subTasksIds.remove((Integer) subTaskId);
    }

    @Override
    public String toString() {
        String result = "EpicTask{"
                + "name='" + this.getName() +"', ";
        if (this.getDescription() == null) {
            result += "description=null, ";
        } else {
            result += "description.length=" + this.getDescription().length() + ", ";
        }
        result += "taskId=" + this.getTaskId() + ", status='" + this.getStatus() + "', ";
        if (this.subTasksIds.isEmpty()) {
            result += "subTasksIds.isEmpty}";
        } else {
            result += "subTasksIds=[" + this.subTasksIds.get(0);
            for (int i = 1; i < this.subTasksIds.size(); i++) {
                result += ", " + this.subTasksIds.get(i);
            }
            result +="]}";
        }
        return result;
    }
}