package com.pathprep.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for generating a detailed learning roadmap.
 * Includes role, experience level, and optional filters for personalization.
 */
@Data
public class DetailedRoadmapRequest {
    @NotBlank(message = "Role is required")
    private String role;
    
    @NotBlank(message = "Experience level is required")
    private String experienceLevel; // beginner, intermediate, advanced
    
    private List<String> currentSkills; // optional
    private Integer timelineWeeks; // optional, default based on level
    private String focusArea; // optional specialization
    
    /**
     * Generates a composite key for caching purposes.
     * Format: {role}_{experienceLevel}
     */
    public String getCompositeKey() {
        return String.format("%s_%s", 
            role.toLowerCase().replace(" ", "_"), 
            experienceLevel.toLowerCase()
        );
    }
}
