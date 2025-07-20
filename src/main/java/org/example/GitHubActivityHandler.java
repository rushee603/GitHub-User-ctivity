package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubActivityHandler {

    private static final String GITHUB_API_URL = "https://api.github.com/users/";

    public void fetchAndDisplayActivity(String username) {

        try {

         URL url = new URL(GITHUB_API_URL + username + "/events");

         HttpURLConnection com = (HttpURLConnection) url.openConnection();

            com.setRequestMethod("GET");
            com.setRequestProperty("Accept", "application/vdn.github.v3+json");

            int status = com.getResponseCode();

            if (status == 404) {
                System.out.println("User not found:" + username);
                return;
            } else if (status != 200) {
                System.out.println("Fail to fetch activity. HTTP error code:" + status);
                return;
            }

         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(com.getInputStream()));
         StringBuilder jsonBuilder = new StringBuilder();
         String line;

         while ((line = bufferedReader.readLine()) != null) {
          jsonBuilder.append(line);
         }

            bufferedReader.close();
            com.disconnect();

            parseAndPrintActivity(jsonBuilder.toString());


        } catch (Exception e) {
            System.out.println("Error fetching activity:" + e.getMessage());
        }
    }

    private void parseAndPrintActivity(String json) {

        Pattern typePattern = Pattern.compile("\"type\":\"(.*?)\"");
        Pattern repoPattern = Pattern.compile("\"repo\":\\{\"id\":.*?,\"name\":\"(.*?)\"");

        Matcher typeMatcher = typePattern.matcher(json);
        Matcher repoMatcher = repoPattern.matcher(json);

        while (typeMatcher.find() && repoMatcher.find()) {
            String type = typeMatcher.group(1);
            String repo = repoMatcher.group(1);
            String action = mapEventTypeToAction(type);

            System.out.println("- " + action + " " + repo);
        }

    }

    private String mapEventTypeToAction(String type) {

        return switch (type) {
            case "PushEvent" -> "Pushed code to";
            case "IssuesEvent" -> "Opened/Closed an issue in";
            case "WatchEvent" -> "Starred";
            case "CreateEvent" -> "Created something in";
            default -> type + " in";
        };
    }


}
