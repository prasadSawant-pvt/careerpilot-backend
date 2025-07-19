package com.pathprep.dto.claude;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
public class ClaudeRequest {
    private String model;
    private List<Message> messages;
    private double temperature = 0.7;
    private int max_tokens = 2000;

    public ClaudeRequest(String model, List<Message> messages, double temperature, int max_tokens) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.max_tokens = max_tokens;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Setter
    @Getter
    public static class Message {
        private String role;
        private String content;

        public void setRole(String role) { this.role = role; }
        public void setContent(String content) { this.content = content; }

    }
}
