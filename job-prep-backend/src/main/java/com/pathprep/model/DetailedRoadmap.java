package com.pathprep.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a detailed learning roadmap for a specific role and experience level.
 * This document is stored in MongoDB and contains the complete learning path
 * with phases, topics, and subtopics.
 */
@Data
@Document(collection = "detailed_roadmaps")
public class DetailedRoadmap {
    
    @Id
    private String id;
    
    /**
     * The role this roadmap is for (e.g., "Java Developer", "Data Scientist")
     */
    private String role;
    
    /**
     * The experience level (e.g., "beginner", "intermediate", "advanced")
     */
    private String experienceLevel;
    
    /**
     * Composite key in format "{role}_{experienceLevel}" for efficient lookups
     */
    @Indexed(unique = true)
    private String compositeKey;
    
    /**
     * Estimated duration of the roadmap in weeks
     */
    private Integer estimatedWeeks;
    
    /**
     * The learning phases of the roadmap
     */
    private List<RoadmapPhase> phases;
    
    /**
     * List of required skills for this roadmap
     */
    private List<String> requiredSkills;
    
    /**
     * Prerequisites before starting this roadmap
     */
    private List<String> prerequisites;
    
    /**
     * Additional metadata about the roadmap
     */
    private Map<String, Object> metadata;
    
    /**
     * Timestamp when the roadmap was created
     */
    @CreatedDate
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the roadmap was last updated
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    /**
     * Sets the composite key based on role and experience level
     */
    public void setCompositeKey() {
        if (this.role != null && this.experienceLevel != null) {
            this.compositeKey = String.format("%s_%s", 
                this.role.toLowerCase().replace(" ", "_"),
                this.experienceLevel.toLowerCase()
            );
        }
    }
    
    /**
     * Updates the composite key and returns the instance for method chaining
     */
    public DetailedRoadmap withCompositeKey() {
        setCompositeKey();
        return this;
    }

    /**
     * Sets the phases from a list of maps, converting each map to a RoadmapPhase
     * @param phaseMaps List of phase maps to convert to RoadmapPhase objects
     */
    public void setPhases(List<Map<String, Object>> phaseMaps) {
        if (phaseMaps == null) {
            this.phases = null;
            return;
        }
        
        // Convert List<Map> to List<RoadmapPhase>
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        this.phases = phaseMaps.stream()
            .map(phaseMap -> {
                // Map 'title' to 'phaseName' if needed
                if (phaseMap.containsKey("title") && !phaseMap.containsKey("phaseName")) {
                    phaseMap.put("phaseName", phaseMap.get("title"));
                }
                
                // Convert the map to RoadmapPhase
                return mapper.convertValue(phaseMap, RoadmapPhase.class);
            })
            .toList();
            
        // Update estimated weeks based on phases if not set
        if (this.estimatedWeeks == null && !this.phases.isEmpty()) {
            this.estimatedWeeks = this.phases.stream()
                .mapToInt(phase -> phase.getWeekNumber() != null ? phase.getWeekNumber() : 1)
                .max()
                .orElse(1);
        }
    }
}
