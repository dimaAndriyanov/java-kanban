package kanban.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kanban.model.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final ZonedDateTime startTime = ZonedDateTime.of(
            2023,
            3,
            4,
            15,
            0,
            0,
            0,
            ZoneId.of("UTC+03:00"));
    public static final Gson gson = (new GsonBuilder())
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
            .create();
    public static KVServer kvServer;
    public static HttpTaskServer taskServer;
    public static HttpClient client = HttpClient.newHttpClient();
    public HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
    public String uri = "http://localhost:8080/tasks";
    public URI requestURI;

    @BeforeAll
    public static void setServers() throws IOException, InterruptedException {
        kvServer = new KVServer();
        kvServer.start();
        taskServer = new HttpTaskServer();
        taskServer.start();
    }
    @AfterAll
    public static void stopServers() {
        kvServer.stop();
        taskServer.stop();
    }

    public HttpRequest createPostRequest(String json, String path) {
        requestURI = URI.create(uri + path);
        return HttpRequest
                .newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json, DEFAULT_CHARSET))
                .uri(requestURI)
                .build();
    }
    public HttpRequest createGetRequest(String path) {
        requestURI = URI.create(uri + path);
        return HttpRequest.newBuilder().GET().uri(requestURI).build();
    }
    public HttpRequest createDeleteRequest(String path) {
        requestURI = URI.create(uri + path);
        return HttpRequest.newBuilder().DELETE().uri(requestURI).build();
    }

    @Test
    public void badRequestTest() throws IOException, InterruptedException{
        String json = gson.toJson(new Task("a", "b"));
        HttpResponse<String> response;

        response = client.send(createPostRequest(json, ""), handler);
        assertEquals(400, response.statusCode());

        response = client.send(createPostRequest(json, "/task"), handler);
        assertEquals(400, response.statusCode());

        response = client.send(createPostRequest(json, "/epic?action=set"), handler);
        assertEquals(400, response.statusCode());

        response = client.send(createPostRequest(json, "/subtask?performance=create"), handler);
        assertEquals(400, response.statusCode());

        response = client.send(createGetRequest(""), handler);
        assertEquals(400, response.statusCode());

        response = client.send(createGetRequest("/task/id200"), handler);
        assertEquals(400, response.statusCode());

        response = client.send(createGetRequest("/task/tasks/all"), handler);
        assertEquals(400, response.statusCode());

        response = client.send(createGetRequest("/subtask/id1"), handler);
        assertEquals(400, response.statusCode());

        response = client.send(createDeleteRequest(""), handler);
        assertEquals(400, response.statusCode());

        response = client.send(createDeleteRequest("/task/id2"), handler);
        assertEquals(400, response.statusCode());

        response = client.send(createDeleteRequest("/task/tasks/all"), handler);
        assertEquals(400, response.statusCode());
    }

    @Test
    public void endpointsTest() throws IOException, InterruptedException {
        HttpResponse<String> response;

        Task task1 = new Task("a", "b");
        task1.setTimeProperties(startTime, 10);
        client.send(createPostRequest(gson.toJson(task1), "/task?action=create"), handler);
        task1.setTaskId(1);
        task1.changeZoneId(ZoneId.of("UTC"));

        EpicTask epic1 = new EpicTask("c", "d");
        client.send(createPostRequest(gson.toJson(epic1), "/epic?action=create"), handler);
        epic1.setTaskId(2);
        epic1.changeZoneId(ZoneId.of("UTC"));

        SubTask sub1 = new SubTask("e", "f", 2);
        sub1.setTimeProperties(startTime.minusMinutes(20), 10);
        client.send(createPostRequest(gson.toJson(sub1), "/subtask?action=create"), handler);
        sub1.setTaskId(3);
        sub1.changeZoneId(ZoneId.of("UTC"));
        epic1.addSubTaskId(3);

        Task task2 = new Task("g", "h");
        client.send(createPostRequest(gson.toJson(task2), "/task?action=create"), handler);
        task2.setTaskId(4);
        task2.changeZoneId(ZoneId.of("UTC"));

        SubTask sub2 = new SubTask("i", "j", 2);
        sub2.setTimeProperties(startTime.plusMinutes(20), 10);
        client.send(createPostRequest(gson.toJson(sub2), "/subtask?action=create"), handler);
        sub2.setTaskId(5);
        sub2.changeZoneId(ZoneId.of("UTC"));
        epic1.addSubTaskId(5);
        epic1.setTimeProperties(sub1.getStartTime(), 50);

        SubTask badSubTask = new SubTask("k", "l", 6);
        response = client.send(createPostRequest(gson.toJson(badSubTask), "/subtask?action=create"), handler);
        assertEquals(404, response.statusCode());

        String wrongJson = "{\"name\"}";
        response = client.send(createPostRequest(wrongJson, "/task?action=create"), handler);
        assertEquals(400, response.statusCode());

        response = client.send(createGetRequest("/task/6"), handler);
        assertEquals(404, response.statusCode());

        response = client.send(createGetRequest("/task/1"), handler);
        assertEquals(gson.toJson(task1), response.body());

        response = client.send(createPostRequest(gson.toJson(badSubTask), "/task?action=update"), handler);
        assertEquals(404, response.statusCode());

        Task badTask = new Task("m", "n");
        badTask.setTaskId(2);
        response = client.send(createPostRequest(gson.toJson(badTask), "/task?action=update"), handler);
        assertEquals(400, response.statusCode());

        response = client.send(createGetRequest("/task/4"), handler);
        Task updatedTask = gson.fromJson(response.body(), Task.class);
        updatedTask.setDescription("newDescription");
        client.send(createPostRequest(gson.toJson(updatedTask), "/task?action=update"), handler);
        response = client.send(createGetRequest("/task/4"), handler);
        assertEquals(response.body(), gson.toJson(updatedTask));

        List<Task> history = new ArrayList<>();
        history.add(task1);
        history.add(updatedTask);
        response = client.send(createGetRequest("/history"), handler);
        assertEquals(gson.toJson(history), response.body());

        List<Task> allTasks = new ArrayList<>();
        allTasks.add(task1);
        allTasks.add(updatedTask);
        allTasks.add(epic1);
        allTasks.add(sub1);
        allTasks.add(sub2);
        response = client.send(createGetRequest("/task/all"), handler);
        assertEquals(gson.toJson(allTasks), response.body());

        List<Task> prioritized = new ArrayList<>();
        prioritized.add(sub1);
        prioritized.add(task1);
        prioritized.add(sub2);
        prioritized.add(updatedTask);
        prioritized.add(epic1);
        response = client.send(createGetRequest("/task/prioritized"), handler);
        assertEquals(gson.toJson(prioritized), response.body());

        response = client.send(createGetRequest("/subtask/6"), handler);
        assertEquals(404, response.statusCode());

        response = client.send(createGetRequest("/subtask/1"), handler);
        assertEquals(400, response.statusCode());

        List<SubTask> allSubs = new ArrayList<>();
        allSubs.add(sub1);
        allSubs.add(sub2);
        response = client.send(createGetRequest("/subtask/2"), handler);
        assertEquals(gson.toJson(allSubs), response.body());

        response = client.send(createDeleteRequest("/task/6"), handler);
        assertEquals(404, response.statusCode());

        client.send(createDeleteRequest("/task/4"), handler);
        allTasks.remove(updatedTask);
        response = client.send(createGetRequest("/task/all"), handler);
        assertEquals(gson.toJson(allTasks), response.body());

        client.send(createDeleteRequest("/task/all"), handler);
        allTasks.clear();
        response = client.send(createGetRequest("/task/all"), handler);
        assertEquals(gson.toJson(allTasks), response.body());
    }
}