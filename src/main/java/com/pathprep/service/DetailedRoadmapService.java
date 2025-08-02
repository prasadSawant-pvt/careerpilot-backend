package com.pathprep.service;

import com.pathprep.dto.DetailedRoadmapRequest;
import com.pathprep.dto.response.DetailedRoadmapResponse;
import com.pathprep.model.DetailedRoadmap;
import reactor.core.publisher.Mono;

/**
 * Service interface for managing detailed learning roadmaps.
 * Handles business logic for roadmap generation, retrieval, and management.
 */
public interface DetailedRoadmapService {
    
    /**
     * Generate or retrieve a detailed learning roadmap.
     * 
     * @param request The roadmap generation request
     * @return A Mono containing the detailed roadmap response
     */
    Mono<DetailedRoadmapResponse> generateOrGetRoadmap(DetailedRoadmapRequest request);
    
    /**
     * Get a roadmap by its composite key (role_experienceLevel).
     * 
     * @param compositeKey The composite key in format "role_experienceLevel"
     * @return A Mono containing the roadmap if found, or empty if not found
     */
    Mono<DetailedRoadmapResponse> getRoadmapByCompositeKey(String compositeKey);
    
    /**
     * Save or update a roadmap.
     * 
     * @param roadmap The roadmap to save
     * @return A Mono containing the saved roadmap
     */
    Mono<DetailedRoadmap> saveRoadmap(DetailedRoadmap roadmap);
    
    /**
     * Delete a roadmap by its ID.
     * 
     * @param id The ID of the roadmap to delete
     * @return A Mono that completes when the roadmap is deleted
     */
    Mono<Void> deleteRoadmap(String id);
    
    /**
     * Generate a new roadmap using AI.
     * 
     * @param request The roadmap generation request
     * @return A Mono containing the generated roadmap
     */
    Mono<DetailedRoadmap> generateRoadmapWithAI(DetailedRoadmapRequest request);
}
