package com.pathprep.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request object for direct Groq model query")
public class GroqQueryRequest {
    @NotBlank(message = "Prompt cannot be blank")
    private String prompt;

    private String model = "llama3-8b-8192"; // Default model

    // Constructors
    public GroqQueryRequest() {}

    public GroqQueryRequest(String prompt) {
        this.prompt = prompt;
    }

    public GroqQueryRequest(String prompt, String model) {
        this.prompt = prompt;
        this.model = model;
    }
}
