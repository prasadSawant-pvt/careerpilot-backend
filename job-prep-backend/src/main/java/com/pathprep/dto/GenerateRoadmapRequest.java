package com.pathprep.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request object for generating a new roadmap")
public class GenerateRoadmapRequest {
    @Schema(description = "The role for which to generate the roadmap", example = "Java Developer", required = true)
    private String role;
    
    @Schema(description = "Experience level for the roadmap", example = "Beginner", required = true)
    private String experience;
    
    @Schema(description = "List of skills (optional)", example = "[\"Java\", \"Spring Boot\"]")
    private List<String> skills;
}
