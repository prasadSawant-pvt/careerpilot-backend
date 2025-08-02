package com.pathprep.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
@Schema(description = "Request object for generating skill-specific interview questions")
public class SkillQuestionsRequest {

    @NotBlank(message = "Skill is required")
    @Schema(description = "Skill for which questions should be generated",
            example = "Spring Boot",
            required = true)
    private String skill;

    @NotBlank(message = "Job role is required")
    @Schema(description = "Job role for context",
            example = "Java Developer",
            required = true)
    private String jobRole;

    @Schema(description = "Years of experience level",
            example = "3-5",
            defaultValue = "1-3")
    private String experienceLevel = "1-3";

    @Schema(description = "Number of questions to generate (max 20)",
            example = "10",
            defaultValue = "10")
    @PositiveOrZero(message = "Number of questions must be positive")
    private int count = 10;

    @Schema(description = "Force refresh the cache for this request",
            example = "false",
            defaultValue = "false")
    private boolean forceRefresh = false;
}
