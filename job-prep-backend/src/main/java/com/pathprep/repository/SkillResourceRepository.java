package com.pathprep.repository;

import com.pathprep.model.SkillResource;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for SkillResource documents
 */
@Repository
public interface SkillResourceRepository extends ReactiveMongoRepository<SkillResource, String> {
    
    /**
     * Find a skill resource by skill name, role, and experience level
     */
    @Query("{'skillName': ?0, 'role': ?1, 'experienceLevel': ?2}")
    Mono<SkillResource> findBySkillNameAndRoleAndExperienceLevel(
        String skillName, String role, String experienceLevel);
    
    /**
     * Find all skill resources for a given role and experience level
     */
    @Query("{'role': ?0, 'experienceLevel': ?1}")
    Flux<SkillResource> findByRoleAndExperienceLevel(String role, String experienceLevel);
    
    /**
     * Check if a skill resource exists for the given skill, role, and experience level
     */
    @Query(value = "{'skillName': ?0, 'role': ?1, 'experienceLevel': ?2}", exists = true)
    Mono<Boolean> existsBySkillNameAndRoleAndExperienceLevel(
        String skillName, String role, String experienceLevel);
}
