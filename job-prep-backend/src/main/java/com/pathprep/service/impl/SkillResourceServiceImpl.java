package com.pathprep.service.impl;

import com.pathprep.config.GroqProperties;
import com.pathprep.dto.SkillResourceRequest;
import com.pathprep.dto.response.SkillResourceResponse;
import com.pathprep.exception.ResourceNotFoundException;
import com.pathprep.exception.ServiceUnavailableException;
import com.pathprep.model.SkillResource;
import com.pathprep.repository.SkillResourceRepository;
import com.pathprep.service.FallbackService;
import com.pathprep.service.GroqAIService;
import com.pathprep.service.SkillResourceService;
import com.pathprep.util.ModelMapperUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Implementation of SkillResourceService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillResourceServiceImpl implements SkillResourceService {

    private final SkillResourceRepository skillResourceRepository;
    private final GroqAIService groqAIService;
    private final GroqProperties groqProperties;
    private final ModelMapperUtil modelMapper;
    private final FallbackService fallbackService;

    // Timeout constants
    private static final Duration DATABASE_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration AI_GENERATION_TIMEOUT = Duration.ofSeconds(30);
    
    // Error messages
    private static final String DB_ERROR_MSG = "Database operation failed";
    private static final String AI_ERROR_MSG = "AI service is currently unavailable";
    private static final String TIMEOUT_MSG = "Operation timed out";

    @Override
    @Cacheable(value = "skillResources", key = "#request.skillName + '_' + #request.role + '_' + #request.experienceLevel")
    @Retryable(retryFor = {DataAccessException.class, TimeoutException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 1000, multiplier = 2))
    @TimeLimiter(name = "skillResourcesService")
    @CircuitBreaker(name = "skillResourcesService", fallbackMethod = "fallbackGetOrGenerateSkillResources")
    public Mono<SkillResourceResponse> getOrGenerateSkillResources(SkillResourceRequest request) {
        String cacheKey = String.format("%s_%s_%s", 
            request.getSkillName(), request.getRole(), request.getExperienceLevel());
        log.info("Processing skill resources request for key: {}", cacheKey);
        
        return skillResourceRepository
            .findBySkillNameAndRoleAndExperienceLevel(
                request.getSkillName(), 
                request.getRole(), 
                request.getExperienceLevel())
            .timeout(DATABASE_TIMEOUT)
            .switchIfEmpty(Mono.defer(() -> {
                log.info("No existing resources found, generating new ones for key: {}", cacheKey);
                return generateSkillResourcesWithAI(request);
            }))
            .map(resource -> convertToResponse((SkillResource) resource))
            .onErrorResume(e -> handleSkillResourceError(e, cacheKey, request));
    }

    @Override
    @Cacheable(value = "skillResources", key = "#id")
    @Retryable(retryFor = {DataAccessException.class, TimeoutException.class}, 
               maxAttempts = 2, 
               backoff = @Backoff(delay = 1000))
    @TimeLimiter(name = "getSkillResourcesByIdService")
    @CircuitBreaker(name = "getSkillResourcesByIdService", fallbackMethod = "fallbackGetSkillResourcesById")
    public Mono<SkillResourceResponse> getSkillResourcesById(String id) {
        log.debug("Fetching skill resources by ID: {}", id);
        
        return skillResourceRepository.findById(id)
            .timeout(DATABASE_TIMEOUT)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Skill resources not found with id: " + id)))
            .map(resource -> convertToResponse((SkillResource) resource))
            .onErrorResume(e -> {
                log.error("Error fetching skill resources by ID: {}", id, e);
                return fallbackService.handleDatabaseError(e, "Failed to fetch skill resources");
            });
    }

    @Override
    @CacheEvict(value = "skillResources", key = "#id")
    public Mono<Void> deleteSkillResources(String id) {
        return skillResourceRepository.deleteById(id);
    }

    @Override
    @CacheEvict(value = "skillResources", key = "#id")
    @Retryable(retryFor = {DataAccessException.class, TimeoutException.class}, 
               maxAttempts = 2, 
               backoff = @Backoff(delay = 1000))
    @TimeLimiter(name = "refreshSkillResourcesService")
    public Mono<SkillResourceResponse> refreshSkillResources(String id) {
        log.info("Refreshing skill resources for ID: {}", id);
        
        return skillResourceRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Skill resources not found with id: " + id)))
            .flatMap(existing -> {
                SkillResourceRequest request = new SkillResourceRequest();
                request.setSkillName(existing.getSkillName());
                request.setRole(existing.getRole());
                request.setExperienceLevel(existing.getExperienceLevel());
                
                return generateSkillResourcesWithAI(request)
                    .timeout(DATABASE_TIMEOUT)
                    .flatMap(updated -> {
                        updated.setId(existing.getId());
                        updated.setCreatedAt(existing.getCreatedAt());
                        return skillResourceRepository.save(updated)
                            .timeout(DATABASE_TIMEOUT);
                    });
            })
            .map(resource -> convertToResponse((SkillResource) resource))
            .onErrorResume(e -> {
                log.error("Failed to refresh skill resources for ID: {}", id, e);
                return fallbackService.handleDatabaseError(e, "Failed to refresh skill resources");
            });
    }

    /**
     * Fallback method for getOrGenerateSkillResources when the circuit is open
     */
    public Mono<SkillResourceResponse> fallbackGetOrGenerateSkillResources(
            SkillResourceRequest request, Throwable t) {
        log.warn("Using fallback for skill resources: {}", t.getMessage());
        return fallbackService.handleDatabaseError(t, 
            createFallbackResponse(request), 
            "getOrGenerateSkillResources");
    }

    /**
     * Handles errors during skill resource processing
     */
    private Mono<SkillResourceResponse> handleSkillResourceError(Throwable e, String cacheKey, SkillResourceRequest request) {
        log.error("Error processing skill resources for key: {}", cacheKey, e);
        
        if (e instanceof TimeoutException) {
            log.warn("Timeout while processing skill resources for key: {}", cacheKey);
            return fallbackService.handleErrorWithDefault(
                e, 
                createFallbackResponse(request),
                "skillResourceTimeout"
            );
        } else if (e instanceof DataAccessException) {
            log.error("Database error while processing skill resources for key: {}", cacheKey, e);
            return fallbackService.handleDatabaseError(e, DB_ERROR_MSG);
        }
        
        return Mono.error(new ServiceUnavailableException("Service temporarily unavailable. Please try again later.", e));
    }

    /**
     * Creates a fallback response when primary services are unavailable
     */
    private SkillResourceResponse createFallbackResponse(SkillResourceRequest request) {
        // Create a minimal response with basic information
        SkillResource fallback = new SkillResource();
        fallback.setId("fallback-" + UUID.randomUUID().toString());
        fallback.setSkillName(request.getSkillName());
        fallback.setRole(request.getRole());
        fallback.setExperienceLevel(request.getExperienceLevel());
        fallback.setCreatedAt(LocalDateTime.now());
        fallback.setUpdatedAt(LocalDateTime.now());
        fallback.setResources(Collections.emptyList());
        fallback.setFallback(true);
        
        return convertToResponse(fallback);
    }

    /**
     * Generates skill resources using AI with retry and fallback
     */
    @Retryable(retryFor = {Exception.class}, 
              maxAttempts = 2, 
              backoff = @Backoff(delay = 1000))
    @TimeLimiter(name = "aiGenerationService")
    private Mono<SkillResource> generateSkillResourcesWithAI(SkillResourceRequest request) {
        log.info("Generating new skill resources with AI for skill: {}, role: {}, level: {}", 
            request.getSkillName(), request.getRole(), request.getExperienceLevel());
        
        String prompt = buildPrompt(request);
        String model = groqProperties.getDefaultModel();
        
        return Mono.defer(() -> groqAIService.generateStructuredResponse(prompt, model, SkillResource.class))
            .timeout(AI_GENERATION_TIMEOUT)
            .flatMap(skillResource -> {
                // Set additional fields
                skillResource.setId(UUID.randomUUID().toString());
                skillResource.setSkillName(request.getSkillName());
                skillResource.setRole(request.getRole());
                skillResource.setExperienceLevel(request.getExperienceLevel());
                skillResource.setCreatedAt(LocalDateTime.now());
                skillResource.setUpdatedAt(LocalDateTime.now());
                skillResource.setFallback(false);
                
                return skillResourceRepository.save(skillResource)
                    .timeout(DATABASE_TIMEOUT)
                    .onErrorResume(e -> {
                        log.error("Failed to save generated resources: {}", e.getMessage());
                        return Mono.just(skillResource); // Return unsaved resource if save fails
                    });
            })
            .onErrorResume(e -> {
                log.error("AI generation failed: {}", e.getMessage());
                return Mono.error(new ServiceUnavailableException(AI_ERROR_MSG, e));
            });
    }
    
    private Mono<SkillResource> combineResources(SkillResource existing, SkillResource updated) {
        // Create a new resource with combined data
        SkillResource combined = new SkillResource();
        combined.setId(existing.getId());
        combined.setSkillName(existing.getSkillName());
        combined.setRole(existing.getRole());
        combined.setExperienceLevel(existing.getExperienceLevel());
        combined.setCreatedAt(existing.getCreatedAt());
        combined.setUpdatedAt(LocalDateTime.now());
        
        // For each resource type, combine existing and updated resources
        combined.setLearningPaths(combineResourceLists(
            existing.getLearningPaths(), updated.getLearningPaths()));
        combined.setProjects(combineResourceLists(
            existing.getProjects(), updated.getProjects()));
        combined.setCertifications(combineResourceLists(
            existing.getCertifications(), updated.getCertifications()));
        combined.setCommunities(combineResourceLists(
            existing.getCommunities(), updated.getCommunities()));
        
        return skillResourceRepository.save(combined);
    }
    
    private <T> List<T> combineResourceLists(List<T> existing, List<T> updated) {
        if (existing == null || existing.isEmpty()) {
            return updated != null ? updated : List.of();
        }
        if (updated == null || updated.isEmpty()) {
            return existing;
        }
        
        // Combine lists and remove duplicates based on title/URL
        Set<String> existingUrls = existing.stream()
            .map(item -> {
                try {
                    return (String) item.getClass().getMethod("getUrl").invoke(item);
                } catch (Exception e) {
                    return "";
                }
            })
            .collect(Collectors.toSet());
        
        List<T> newItems = updated.stream()
            .filter(item -> {
                try {
                    String url = (String) item.getClass().getMethod("getUrl").invoke(item);
                    return url != null && !existingUrls.contains(url);
                } catch (Exception e) {
                    return false;
                }
            })
            .collect(Collectors.toList());
        
        List<T> result = new ArrayList<>(existing);
        result.addAll(newItems);
        return result;
    }
    
    private boolean shouldUpdateWithAI(SkillResource resource) {
        // Update if the resource is older than 30 days
        if (resource.getUpdatedAt() != null) {
            return Duration.between(resource.getUpdatedAt(), LocalDateTime.now()).toDays() > 30;
        }
        // Update if we don't have an update timestamp
        return true;
    }
    
    private String buildPrompt(SkillResourceRequest request) {
        return String.format("""
            Generate a comprehensive list of learning resources for the skill: %s
            Target role: %s
            Experience level: %s
            
            Please provide resources in the following categories:
            - Learning Paths: %s
            - Projects: %s
            - Certifications: %s
            - Communities: %s
            
            For each resource, include:
            - Title
            - URL
            - Brief description
            - Type (FREE/PAID/COMMUNITY)
            - Level (BEGINNER/INTERMEDIATE/ADVANCED)
            - Estimated hours to complete (if applicable)
            - Rating (1-5, if available)
            
            Format the response as a JSON object matching the SkillResource model structure.
            """,
            request.getSkillName(),
            request.getRole(),
            request.getExperienceLevel(),
            request.getIncludeLearningPaths() ? "Include" : "Exclude",
            request.getIncludeProjects() ? "Include" : "Exclude",
            request.getIncludeCertifications() ? "Include" : "Exclude",
            request.getIncludeCommunities() ? "Include" : "Exclude"
        );
    }
    
    private SkillResourceResponse convertToResponse(SkillResource resource) {
        return modelMapper.map(resource, SkillResourceResponse.class);
    }
}
