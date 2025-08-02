package com.pathprep.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for generating a detailed learning roadmap.
 * Includes role, experience level, and optional filters for personalization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedRoadmapRequest {
    @NotBlank(message = "Role is required")
    private String role;
    
    @NotBlank(message = "Experience level is required")
    private String experienceLevel; // beginner, intermediate, advanced
    
    @Builder.Default
    private List<String> currentSkills = new ArrayList<>(); // optional
    
    private Integer timelineWeeks; // optional, default based on level
    
    private String focusArea; // optional specialization

    /**
     * If true, forces regeneration of the roadmap even if it exists
     */
    @Schema(description = "If true, forces regeneration of the roadmap even if one exists", example = "false", defaultValue = "false")
    @Builder.Default
    private boolean forceRegenerate = false;

    /**
     * Generates a composite key for caching purposes.
     * Format: {role}_{experienceLevel}
     */
    public String getCompositeKey() {
        return String.format("%s_%s_%d", 
            role.toLowerCase().replace(" ", "_"), 
            experienceLevel.toLowerCase(),
            timelineWeeks != null ? timelineWeeks : 0
        );
    }
}
