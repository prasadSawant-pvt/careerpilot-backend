package com.pathprep.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Represents a learning resource associated with a subtopic.
 * Can be a book, article, video, or other educational material.
 */
@Data
@Document
public class LearningResource {
    private String title;
    private String description;
    private String url;
    private String type; // book, article, video, course, tutorial, documentation
    private String provider; // e.g., YouTube, Udemy, official docs
    private Boolean isFree;
    private String estimatedDuration; // e.g., "2h 30m"
    private String difficulty; // beginner, intermediate, advanced
    private List<String> tags;
    private Double rating; // 1-5 if available
    
    // Additional metadata
    private String author;
    private Integer publicationYear;
    private String language;
    
    /**
     * Checks if this is a free resource
     */
    public boolean isFreeResource() {
        return Boolean.TRUE.equals(isFree);
    }
}
