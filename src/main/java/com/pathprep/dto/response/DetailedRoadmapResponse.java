package com.pathprep.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pathprep.model.RoadmapPhase;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for detailed roadmap generation.
 * Contains comprehensive learning path with hierarchical structure.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetailedRoadmapResponse {
    private String id;
    private String role;
    private String experienceLevel;
    private String compositeKey;
    private Integer estimatedWeeks;
    private List<RoadmapPhase> phases;
    private List<String> requiredSkills;
    private List<String> prerequisites;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
