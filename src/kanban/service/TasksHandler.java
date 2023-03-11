package kanban.service;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import kanban.model.*;
import kanban.exceptions.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.OptionalInt;

public class TasksHandler implements HttpHandler {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final Gson gson = (new GsonBuilder())
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
            .create();
    private final TaskManager taskManager;

    TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange);
        switch(endpoint) {
            case GET_TASK: {
                handleGetTask(exchange);
                break;
            }
            case GET_ALL_TASKS: {
                handleGetAllTasks(exchange);
                break;
            }
            case GET_PRIORITIZED_TASKS: {
                handleGetPrioritizedTasks(exchange);
                break;
            }
            case GET_ALL_SUBTASKS: {
                handleGetAllSubtasks(exchange);
                break;
            }
            case CREATE_TASK: {
                handleCreateTask(exchange);
                break;
            }
            case UPDATE_TASK: {
                handleUpdateTask(exchange);
                break;
            }
            case DELETE_TASK: {
                handleDeleteTask(exchange);
                break;
            }
            case DELETE_ALL_TASKS: {
                handleDeleteAllTasks(exchange);
                break;
            }
            case GET_HISTORY: {
                handleGetHistory(exchange);
                break;
            }
            default: {
                writeResponse(exchange, "Запрос сформирован неверно", 400);
            }
        }
    }

    private static Endpoint getEndpoint(HttpExchange exchange) {
        String[] requestPathParts = exchange.getRequestURI().getPath().split("/");
        String query = exchange.getRequestURI().getQuery();
        switch (exchange.getRequestMethod()) {
            case "GET": {
                if (requestPathParts.length == 3 && requestPathParts[2].equals("history")) {
                    return Endpoint.GET_HISTORY;
                }
                if (requestPathParts.length == 4) {
                    if (requestPathParts[2].equals("subtask")) {
                        if (getTaskId(exchange).isPresent()) {
                            return Endpoint.GET_ALL_SUBTASKS;
                        } else {
                            return Endpoint.UNSUPPORTED;
                        }
                    }
                    if (requestPathParts[2].equals("task")) {
                        if (requestPathParts[3].equals("all")) {
                            return Endpoint.GET_ALL_TASKS;
                        }
                        if (requestPathParts[3].equals("prioritized")) {
                            return Endpoint.GET_PRIORITIZED_TASKS;
                        }
                        if (getTaskId(exchange).isPresent()) {
                            return Endpoint.GET_TASK;
                        } else {
                            return Endpoint.UNSUPPORTED;
                        }
                    }
                }
                break;
            }
            case "POST": {
                if (requestPathParts.length == 3) {
                    try {
                        if (requestPathParts[2].equals("task")
                                || requestPathParts[2].equals("subtask")
                                || requestPathParts[2].equals("epic")) {
                            if (query != null && !query.contains("&")) {
                                String[] queryStringParameter = query.split("=");
                                if (queryStringParameter[0].equals("action")) {
                                    if (queryStringParameter[1].equals("create")) {
                                        return Endpoint.CREATE_TASK;
                                    }
                                    if (queryStringParameter[1].equals("update")) {
                                        return Endpoint.UPDATE_TASK;
                                    }
                                }
                            }
                        }
                    } catch (IndexOutOfBoundsException exception) {
                        return Endpoint.UNSUPPORTED;
                    }
                }
                break;
            }
            case "DELETE": {
                if (requestPathParts.length == 4 && requestPathParts[2].equals("task")) {
                    if (requestPathParts[3].equals("all")) {
                        return Endpoint.DELETE_ALL_TASKS;
                    }
                    if (getTaskId(exchange).isPresent()) {
                        return Endpoint.DELETE_TASK;
                    } else {
                        return Endpoint.UNSUPPORTED;
                    }
                }
            }
        }
        return Endpoint.UNSUPPORTED;
    }

    private static OptionalInt getTaskId(HttpExchange exchange) {
        OptionalInt result;
        try {
            result = OptionalInt.of(Integer.parseInt(exchange.getRequestURI().getPath().split("/")[3]));
        } catch (NumberFormatException exception) {
            result = OptionalInt.empty();
        }
        return result;
    }

    private static Task getTask(HttpExchange exchange) throws IOException {
        String taskType = exchange.getRequestURI().getPath().split("/")[2];
        Task task;
        switch(taskType) {
            case "task": {
                task = gson.fromJson(
                        new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET),
                        Task.class);
                break;
            }
            case "epic": {
                task = gson.fromJson(
                        new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET),
                        EpicTask.class);
                break;
            }
            case "subtask": {
                task = gson.fromJson(
                        new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET),
                        SubTask.class);
                break;
            }
            default: {
                task = null;
            }
        }
        return task;
    }

    private static void writeResponse(HttpExchange exchange, String responseString, int responseCode)
        throws IOException {
        if (responseCode == 204) {
            exchange.sendResponseHeaders(responseCode, -1);
        } else if (responseString.isBlank()) {
            exchange.sendResponseHeaders(responseCode, 0);
        } else {
            byte[] bytes = responseString.getBytes(DEFAULT_CHARSET);
            exchange.sendResponseHeaders(responseCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
        exchange.close();
    }

    private void handleGetTask(HttpExchange exchange) throws IOException {
        try {
            Task task = taskManager.getTaskByTaskId(getTaskId(exchange).getAsInt());
            String taskJson = gson.toJson(task);
            writeResponse(exchange, taskJson, 200);
        } catch (TaskManagerException exception) {
            writeResponse(exchange, exception.getMessage(), 404);
        } catch (FileBackedTaskManagerException exception) {
            writeResponse(exchange, "На сервере возникла проблема", 500);
        }
    }

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        try {
            String allTasksJson = gson.toJson(taskManager.getAllTasks());
            writeResponse(exchange, allTasksJson, 200);
        } catch (FileBackedTaskManagerException exception) {
            writeResponse(exchange, "На сервере возникла проблема", 500);
        }
    }

    private void handleGetPrioritizedTasks(HttpExchange exchange) throws IOException {
        try {
            String prioritizedTasksJson = gson.toJson(taskManager.getPrioritizedTasks());
            writeResponse(exchange, prioritizedTasksJson, 200);
        } catch (FileBackedTaskManagerException exception) {
            writeResponse(exchange, "На сервере возникла проблема", 500);
        }
    }

    private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
        try {
            int taskId = getTaskId(exchange).getAsInt();
            String allSubTasksJson = gson.toJson(taskManager.getAllSubTasksByEpicTaskId(taskId));
            writeResponse(exchange, allSubTasksJson, 200);
        } catch (NoSuchTaskException exception) {
            writeResponse(exchange, exception.getMessage(), 404);
        } catch (TaskManagerException exception) {
            writeResponse(exchange, exception.getMessage(), 400);
        } catch (FileBackedTaskManagerException exception) {
            writeResponse(exchange, "На сервере возникла проблема", 500);
        }
    }

    private void handleCreateTask(HttpExchange exchange) throws IOException {
        try {
            int result = taskManager.createTask(getTask(exchange));
            writeResponse(exchange, Integer.toString(result), 200);
        } catch (JsonSyntaxException exception) {
            writeResponse(exchange, "Получен некорректный JSON", 400);
        } catch (NoSuchTaskException exception) {
            writeResponse(exchange, exception.getMessage(), 404);
        } catch (TaskManagerException exception) {
            writeResponse(exchange, exception.getMessage(), 400);
        } catch (FileBackedTaskManagerException exception) {
            writeResponse(exchange, "На сервере возникла проблема", 500);
        }
    }

    private void handleUpdateTask(HttpExchange exchange) throws IOException {
        try {
            int result = taskManager.updateTask(getTask(exchange));
            writeResponse(exchange, Integer.toString(result), 200);
        } catch (JsonSyntaxException exception) {
            writeResponse(exchange, "Получен некорректный JSON", 400);
        } catch (NoSuchTaskException exception) {
            writeResponse(exchange, exception.getMessage(), 404);
        } catch (TaskManagerException exception) {
            writeResponse(exchange, exception.getMessage(), 400);
        } catch (FileBackedTaskManagerException exception) {
            writeResponse(exchange, "На сервере возникла проблема", 500);
        }
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        try {
            int result = taskManager.deleteTaskByTaskId(getTaskId(exchange).getAsInt());
            writeResponse(exchange, Integer.toString(result), 200);
        } catch (TaskManagerException exception) {
            writeResponse(exchange, exception.getMessage(), 404);
        } catch (FileBackedTaskManagerException exception) {
            writeResponse(exchange, "На сервере возникла проблема", 500);
        }
    }

    private void handleDeleteAllTasks(HttpExchange exchange) throws IOException {
        try {
            taskManager.deleteAllTasks();
            writeResponse(exchange, "", 204);
        } catch (FileBackedTaskManagerException exception) {
            writeResponse(exchange, "На сервере возникла проблема", 500);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        try {
            String historyJson = gson.toJson(taskManager.getHistory());
            writeResponse(exchange, historyJson, 200);
        } catch (FileBackedTaskManagerException exception) {
            writeResponse(exchange, "На сервере возникла проблема", 500);
        }
    }
}