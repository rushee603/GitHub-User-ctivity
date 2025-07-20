package org.example;

public class Main {

    public static void main(String[] args) {

        if(args.length != 1){
            System.out.println("User: github-activity rusheeeeee");
            return;
        }

        String username = args[0];
        GitHubActivityHandler handler = new GitHubActivityHandler();
        handler.fetchAndDisplayActivity(username);

    }

}