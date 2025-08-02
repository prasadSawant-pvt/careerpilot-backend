package com.pathprep.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Model class for skill resources
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "skill_resources")
@CompoundIndex(def = "{'skillName': 1, 'role': 1, 'experienceLevel': 1}", unique = true, name = "composite_key_idx")
public class SkillResource {
    @Id
    private String id;
    
    private String skillName;
    private String role;
    private String experienceLevel;
    
    private List<ResourceItem> learningPaths;
    private List<ResourceItem> projects;
    private List<ResourceItem> certifications;
    private List<ResourceItem> communities;
    
    private List<ResourceItem> resources;
    private boolean fallback;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public void setResources(List<ResourceItem> resources) {
        this.resources = resources;
    }
    
    public void setFallback(boolean fallback) {
        this.fallback = fallback;
    }
    
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
