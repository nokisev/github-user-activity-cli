package ru.nokisev;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        String username;
        if (args.length > 0) {
            username = args[0];
        } else {
            System.out.println("Input your username in args!");
            return;
        }
        fetchGithubActivity(username);
    }

    public static void fetchGithubActivity(String username) throws IOException, URISyntaxException, InterruptedException {
        String GITHUB_API = String.format("https://api.github.com/users/%s/events", username);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(new URI(GITHUB_API)).header("Accept", "application/vnd.github+json").GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 404) {
            System.out.println("User not found. Please check your username!");
            return;
        }
        if (response.statusCode() == 200) {
            JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
            displayActivity(jsonArray);
            return;
        } else {
            System.out.println("Error:" + response.statusCode());
        }

        client.close();
    }

    private static void displayActivity(JsonArray events) {
        for (JsonElement element : events) {
            JsonObject event = element.getAsJsonObject();
            String type = event.get("type").getAsString();
            String action;
            switch (type){
                case "PushEvent":
                    int commitCount = event.get("payload").getAsJsonObject().get("commits").getAsJsonArray().size();
                    action = "- Pushed " + commitCount + " commit(s) to " + event.get("repo").getAsJsonObject().get("name");
                    break;
                case "IssuesEvent":
                    action = "- " + event.get("payload").getAsJsonObject().get("action").getAsString().toUpperCase().charAt(0)
                            + event.get("payload").getAsJsonObject().get("action").getAsString()
                            + " an issue in ${event.repo.name}";
                    break;
                case "WatchEvent":
                    action = "- Starred " + event.get("repo").getAsJsonObject().get("name").getAsString();
                    break;
                case "ForkEvent":
                    action = "- Forked " + event.get("repo").getAsJsonObject().get("name").getAsString();
                    break;
                case "CreateEvent":
                    action = "- Created " + event.get("payload").getAsJsonObject().get("ref_type").getAsString()
                            + " in " + event.get("repo").getAsJsonObject().get("name").getAsString();

                    break;
                default:
                    action = "- " +  event.get("type").getAsString().replace("Event", "")
                            + " in " + event.get("repo").getAsJsonObject().get("name").getAsString();
                    break;
            }
            System.out.println(action);
        }
    }
}