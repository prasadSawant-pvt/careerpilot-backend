package com.pathprep.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Represents a phase in the learning roadmap.
 * Each phase typically spans one week of learning.
 */
@Data
@Document
public class RoadmapPhase {
    private String phaseName;
    private Integer weekNumber;
    private String objective;
    @JsonProperty("topics")
    private List<Topic> topics;
    private List<String> deliverables;
    
    // Add any additional fields or methods as needed
}
