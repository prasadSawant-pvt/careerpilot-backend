package com.pathprep.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "groq")
public class GroqProperties {
    private String apiKey;
    private String baseUrl;
    private Duration timeout;
    private int maxRetries;
    private Map<String, String> models;

    public String getDefaultModel() {
        return models != null && models.containsKey("default") ? models.get("default") : "llama3-8b-8192";
    }
    public String getCodeModel() {
        return models != null && models.containsKey("code") ? models.get("code") : "codellama-34b-instruct";
    }
}
