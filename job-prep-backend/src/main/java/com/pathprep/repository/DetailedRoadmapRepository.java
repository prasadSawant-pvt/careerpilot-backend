package com.pathprep.repository;

import com.pathprep.model.DetailedRoadmap;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for managing detailed roadmap documents in MongoDB.
 * Provides reactive CRUD operations and custom query methods.
 */
@Repository
public interface DetailedRoadmapRepository extends ReactiveMongoRepository<DetailedRoadmap, String> {
    
    /**
     * Find a roadmap by its composite key (role_experienceLevel).
     * If multiple documents exist with the same composite key, returns the most recent one.
     * 
     * @param compositeKey The composite key to search for (format: role_experienceLevel)
     * @return A Mono containing the most recent matching DetailedRoadmap, or empty if none found
     */
    @Aggregation(pipeline = {
        "{'$match': {'compositeKey': ?0}}",
        "{'$sort': {'createdAt': -1}}",
        "{'$limit': 1}"  
    })
    Mono<DetailedRoadmap> findByCompositeKey(String compositeKey);
    
    /**
     * Find all roadmaps for a specific role
     */
    Flux<DetailedRoadmap> findByRole(String role);
    
    /**
     * Find all roadmaps for a specific experience level
     */
    Flux<DetailedRoadmap> findByExperienceLevel(String experienceLevel);
    
    /**
     * Find all roadmaps containing a specific skill
     */
    Flux<DetailedRoadmap> findByRequiredSkillsContaining(String skill);
    
    /**
     * Check if a roadmap exists for the given composite key
     */
    Mono<Boolean> existsByCompositeKey(String compositeKey);
}
