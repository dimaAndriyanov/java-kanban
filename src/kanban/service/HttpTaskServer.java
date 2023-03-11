package kanban.service;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;

    public HttpTaskServer() throws IOException, InterruptedException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TasksHandler(Managers.getDefault()));
    }

    public void start() {
        server.start();
        System.out.println("HttpTaskServer запущен на порту " + PORT);
    }

    public void stop() {
        server.stop(0);
        System.out.println("HttpTaskServer остановлен");
    }
}