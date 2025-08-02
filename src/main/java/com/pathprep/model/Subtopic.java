package com.pathprep.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Represents a subtopic within a topic.
 * Contains detailed learning points and practice activities.
 */
@Data
@Document
public class Subtopic {
    private String name;
    private String description;
    private List<String> keyPoints;
    private Integer estimatedHours;
    private String practiceType; // theory, hands-on, project
    private List<LearningResource> resources;
    private List<String> learningObjectives;
    private String difficulty; // easy, medium, hard
    
    // Additional fields for tracking progress
    private Boolean completed = false;
    private String notes;
    
    /**
     * Gets the estimated hours, defaulting to 1 if not set
     */
    public Integer getEstimatedHours() {
        return estimatedHours != null ? estimatedHours : 1;
    }
}
