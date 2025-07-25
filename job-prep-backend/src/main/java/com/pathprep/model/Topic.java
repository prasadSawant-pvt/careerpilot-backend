package com.pathprep.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Represents a topic within a roadmap phase.
 * Contains subtopics and learning objectives.
 */
@Data
@Document
public class Topic {
    private String topicName;
    private String description;
    private List<Subtopic> subtopics;
    private Integer estimatedHours;
    private String difficulty; // easy, medium, hard
    private List<String> associatedSkills;
    
    // Additional fields for tracking and metadata
    private String category;
    private List<String> keyConcepts;
    private List<String> learningOutcomes;
    
    /**
     * Calculates total estimated hours including all subtopics
     * @return total estimated hours
     */
    public int getTotalEstimatedHours() {
        int total = estimatedHours != null ? estimatedHours : 0;
        if (subtopics != null) {
            total += subtopics.stream()
                    .mapToInt(st -> st.getEstimatedHours() != null ? st.getEstimatedHours() : 0)
                    .sum();
        }
        return total;
    }
}
