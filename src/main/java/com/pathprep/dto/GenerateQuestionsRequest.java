package com.pathprep.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Request object for generating interview questions")
public class GenerateQuestionsRequest {
    
    @NotBlank(message = "Role is required")
    @Schema(description = "Job role for which questions should be generated", example = "Java Developer")
    private String role;
    
    @NotBlank(message = "Experience level is required")
    @Schema(description = "Experience level (e.g., Entry, Mid, Senior)", example = "Mid")
    private String experienceLevel;
    
    @NotNull(message = "Number of questions is required")
    @Positive(message = "Number of questions must be positive")
    @Schema(description = "Number of questions to generate", example = "10")
    private Integer count;
    
    @Schema(description = "Specific topics to focus on (comma-separated)", 
            example = "Spring Boot, Hibernate, Microservices", 
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String topics;
    
    @Schema(description = "Force refresh the cache for this request", 
            example = "false", 
            defaultValue = "false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean forceRefresh = false;
}
