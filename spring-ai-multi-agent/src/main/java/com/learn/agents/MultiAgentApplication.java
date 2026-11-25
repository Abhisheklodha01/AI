package com.learn.agents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MultiAgentApplication {

    public static void main(String[] args) {
        String apiKey = System.getenv("OPENROUTER_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("ERROR: OPENROUTER_API_KEY environment variable is not set.");
            System.exit(1);
        }
        SpringApplication.run(MultiAgentApplication.class, args);
    }
}