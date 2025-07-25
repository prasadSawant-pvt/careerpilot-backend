package com.pathprep.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for skill resources
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkillResourceResponse {
    private String id;
    private String skillName;
    private String role;
    private String experienceLevel;
    private List<ResourceItem> learningPaths;
    private List<ResourceItem> projects;
    private List<ResourceItem> certifications;
    private List<ResourceItem> communities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Nested class for resource items
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceItem {
        private String title;
        private String url;
        private String description;
        private String type; // e.g., "FREE", "PAID", "COMMUNITY"
        private String level; // e.g., "BEGINNER", "INTERMEDIATE", "ADVANCED"
        private Double rating; // 1-5 rating if available
        private Integer estimatedHours; // Estimated time to complete
    }
}
