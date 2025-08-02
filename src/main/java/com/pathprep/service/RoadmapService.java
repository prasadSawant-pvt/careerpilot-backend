package com.pathprep.service;

import com.pathprep.model.Roadmap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service interface for managing learning roadmaps.
 */
public interface RoadmapService {
    
    /**
     * Generates a new learning roadmap or returns an existing one if available.
     *
     * @param role The target role for the roadmap
     * @param experience The experience level for the roadmap
     * @param skills List of skills to include in the roadmap
     * @return A Mono containing the generated or existing Roadmap
     */
    Mono<Roadmap> generateRoadmap(String role, String experience, List<String> skills);
    
    /**
     * Retrieves a roadmap by its ID.
     *
     * @param id The ID of the roadmap to retrieve
     * @return A Mono containing the found Roadmap, or empty if not found
     */
    Mono<Roadmap> getRoadmap(String id);

    Mono<String> queryGroqModel(String prompt, String model);

    /**
     * Retrieves the most recent roadmaps.
     *
     * @param limit Maximum number of roadmaps to return
     * @return A Flux of recent Roadmaps
     */
    Flux<Roadmap> getRecentRoadmaps(int limit);
    
    /**
     * Retrieves the most popular roadmaps.
     *
     * @param limit Maximum number of roadmaps to return
     * @return A Flux of trending Roadmaps
     */
    Flux<Roadmap> getTrendingRoadmaps(int limit);
    
    /**
     * Saves a roadmap.
     *
     * @param roadmap The roadmap to save
     * @return A Mono containing the saved Roadmap
     */
    Mono<Roadmap> saveRoadmap(Roadmap roadmap);
    
    /**
     * Deletes a roadmap by its ID.
     *
     * @param id The ID of the roadmap to delete
     * @return A Mono that completes when the roadmap is deleted
     */
    Mono<Void> deleteRoadmap(String id);

    /**
     * Retrieves an existing roadmap or generates a new one if not found.
     *
     * @param role The target role for the roadmap
     * @param experience The experience level for the roadmap
     * @return A Mono containing the found or generated Roadmap
     */
    Mono<Roadmap> getOrGenerateRoadmap(String role, String experience);
}
