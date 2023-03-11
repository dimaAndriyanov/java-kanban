package kanban.service;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

public class Managers {
    public static TaskManager getDefault() throws IOException, InterruptedException{
        return getHttpTaskManager(new URL("http://localhost:8078"));
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static InMemoryTaskManager getInMemoryTaskManager() {
        return new InMemoryTaskManager(1, getDefaultHistory());
    }

    public static FileBackedTaskManager getFileBackedTaskManager(String path) {
        return new FileBackedTaskManager(1, getDefaultHistory(),
                Paths.get(path));
    }

    public static HttpTaskManager getHttpTaskManager(URL url)  throws IOException, InterruptedException {
        return new HttpTaskManager(url);
    }
}