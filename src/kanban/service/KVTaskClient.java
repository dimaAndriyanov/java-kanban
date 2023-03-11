package kanban.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
    private final String apiToken;
    private final URI uri;

    public KVTaskClient(URL url) throws IOException, InterruptedException {
        uri = URI.create(url.toString());
        URI requestURI = URI.create(uri + "/register");
        HttpRequest registerRequest = HttpRequest.newBuilder().GET().uri(requestURI).build();
        HttpResponse<String> response = client.send(registerRequest, handler);
        if (response.statusCode() == 200) {
            apiToken = response.body();
        } else {
            throw new IOException();
        }
    }

    public void put(String key, String json) throws IOException, InterruptedException {
        URI requestURI = URI.create(uri + "/save/" + key + "?API_TOKEN=" + apiToken);
        HttpRequest saveRequest = HttpRequest.newBuilder().
                POST(HttpRequest.BodyPublishers.ofString(json, UTF_8)).
                uri(requestURI).
                build();
        HttpResponse<String> response = client.send(saveRequest, handler);
        if (response.statusCode() != 200) {
            throw new IOException();
        }
    }

    public String load(String key) throws IOException, InterruptedException {
        URI requestURI = URI.create(uri + "/load/" + key + "?API_TOKEN=" + apiToken);
        HttpRequest loadRequest = HttpRequest.newBuilder().GET().uri(requestURI).build();
        HttpResponse<String> response = client.send(loadRequest, handler);
        if (response.statusCode() == 404) {
            return "";
        }
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new IOException();
        }
    }
}