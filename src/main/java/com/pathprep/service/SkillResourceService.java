package com.pathprep.service;

import com.pathprep.dto.SkillResourceRequest;
import com.pathprep.dto.response.SkillResourceResponse;
import reactor.core.publisher.Mono;

/**
 * Service interface for managing skill resources
 */
public interface SkillResourceService {
    
    /**
     * Generate or retrieve skill resources
     * @param request The skill resource request
     * @return A Mono containing the skill resource response
     */
    Mono<SkillResourceResponse> getOrGenerateSkillResources(SkillResourceRequest request);
    
    /**
     * Get skill resources by ID
     * @param id The resource ID
     * @return A Mono containing the skill resource response
     */
    Mono<SkillResourceResponse> getSkillResourcesById(String id);
    
    /**
     * Delete skill resources by ID
     * @param id The resource ID
     * @return A Mono indicating completion
     */
    Mono<Void> deleteSkillResources(String id);
    
    /**
     * Refresh skill resources with new AI-generated content
     * @param id The resource ID
     * @return A Mono containing the updated skill resource response
     */
    Mono<SkillResourceResponse> refreshSkillResources(String id);
}
