package kanban.service;

import kanban.exceptions.HttpTaskManagerException;
import kanban.model.*;

import java.io.IOException;
import java.net.URL;

public class HttpTaskManager extends FileBackedTaskManager {
    private final KVTaskClient client;
    private final static String TASK_DIVIDER = "/nxtTsk/";
    public HttpTaskManager(URL url) throws IOException, InterruptedException {
        super(1, Managers.getDefaultHistory(), null);
        client = new KVTaskClient(url);
        loadFromServer();
    }

    private void loadFromServer(){
        try {
            String tasksString = client.load("tasks");
            if (tasksString.isBlank() || tasksString.equals("empty")) {
                return;
            }
            String[] tasksStringParts = tasksString.split(TASK_DIVIDER);
            int nextId = 1;
            for (String taskString : tasksStringParts) {
                Task task = taskFromString(taskString);
                setNextTaskId(task.getTaskId());
                createTaskFromParent(task);
                if (nextId < task.getTaskId() + 1) {
                    nextId = task.getTaskId() + 1;
                }
            }
            setNextTaskId(nextId);
            String history = client.load("history");
            if (history.isBlank() || history.equals("empty")) {
                return;
            }
            addHistoryFromString(history);
        } catch (IOException | InterruptedException exception) {
            throw new HttpTaskManagerException("Could not load manager state due to server issues");
        }
    }

    @Override
    void save() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Task task : getAllTasksNoHistory()) {
            stringBuilder.append(toString(task)).append(TASK_DIVIDER);
        }
        String tasks = stringBuilder.toString();
        String history = historyToString();
        try {
            if (tasks.isBlank()) {
                client.put("tasks", "empty");
            } else {
                client.put("tasks", tasks);
            } if (history.isBlank()) {
                client.put("history", "empty");
            } else {
                client.put("history", history);
            }
        } catch (IOException | InterruptedException exception) {
            throw new HttpTaskManagerException("Could not save manager state due to server issues");
        }
    }
}