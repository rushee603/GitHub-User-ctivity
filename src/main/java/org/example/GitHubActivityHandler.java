package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubActivityHandler {

    private static final String API_USER_URL = "https://api.github.com/users/";

    public void fetchAndDisplayActivity(String username) {

        if (!isValidGitHubUser(username)) {
            System.out.println("Error: '" + username + "' is not a valid GitHub user.");
            return;
        }

        try {
            URL url = new URL(API_USER_URL + username + "/events");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

            int status = conn.getResponseCode();

            if (status == 403) {
                System.out.println("API rate limit exceeded. Try again later or authenticate.");
                return;
            } else if (status != 200) {
                System.out.println("Failed to fetch activity. HTTP Code: " + status);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            reader.close();
            conn.disconnect();

            parseAndPrintActivity(jsonBuilder.toString());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private boolean isValidGitHubUser(String username) {

        try {
            URL url = new URL(API_USER_URL + username);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int status = conn.getResponseCode();
            conn.disconnect();

            return status == 200;

        } catch (Exception e) {
            return false;
        }
    }

    private void parseAndPrintActivity(String json) {

        Pattern typePattern = Pattern.compile("\"type\":\"(.*?)\"");
        Pattern repoPattern = Pattern.compile("\"repo\":\\{\"id\":.*?,\"name\":\"(.*?)\"");

        Matcher typeMatcher = typePattern.matcher(json);
        Matcher repoMatcher = repoPattern.matcher(json);

        boolean found = false;

        while (typeMatcher.find() && repoMatcher.find()) {
            found = true;
            String type = typeMatcher.group(1);
            String repo = repoMatcher.group(1);
            String action = mapEventTypeToAction(type);

            System.out.println("- " + action + " " + repo);
        }

        if (!found) {
            System.out.println("No recent public activity found for user.");
        }
    }

    private String mapEventTypeToAction(String type) {

        return switch (type) {
            case "PushEvent" -> "Pushed commits to";
            case "IssuesEvent" -> "Worked on issue in";
            case "IssueCommentEvent" -> "Commented on issue in";
            case "WatchEvent" -> "Starred";
            case "ForkEvent" -> "Forked";
            case "CreateEvent" -> "Created something in";
            case "PullRequestEvent" -> "Interacted with PR in";
            default -> type + " in";
        };
    }

}
