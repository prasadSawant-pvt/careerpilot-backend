package com.pathprep.repository;

import com.pathprep.model.Roadmap;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for Roadmap documents.
 */
@Repository
public interface RoadmapRepository extends ReactiveMongoRepository<Roadmap, String> {
    
    /**
     * Finds a roadmap by role and experience level.
     *
     * @param role The target role
     * @param experience The experience level
     * @return A Mono containing the found Roadmap, or empty if not found
     */
    @Query("{ 'role': ?0, 'experience': ?1 }")
    Mono<Roadmap> findByRoleAndExperience(String role, String experience);
    
    /**
     * Finds all roadmaps ordered by creation date (newest first).
     *
     * @return A Flux of Roadmaps ordered by creation date
     */
    Flux<Roadmap> findAllByOrderByCreatedAtDesc();
    
    /**
     * Finds all roadmaps ordered by popularity (highest first).
     *
     * @return A Flux of Roadmaps ordered by popularity
     */
    Flux<Roadmap> findAllByOrderByPopularityDesc();
    
    /**
     * Checks if a roadmap exists for the given role and experience level.
     *
     * @param role The target role
     * @param experience The experience level
     * @return A Mono containing true if a matching roadmap exists, false otherwise
     */
    Mono<Boolean> existsByRoleAndExperience(String role, String experience);

    @Override
    Mono<Long> count();
}
