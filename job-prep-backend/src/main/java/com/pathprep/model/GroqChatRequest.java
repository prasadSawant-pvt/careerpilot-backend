package com.pathprep.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroqChatRequest {
    private String model;
    private List<GroqMessage> messages;
    private Double temperature;
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    public static GroqChatRequest simple(String prompt, String model) {
        return new GroqChatRequest(model,
            Collections.singletonList(new GroqMessage("user", prompt)),
            0.7, 1500);
    }
}

