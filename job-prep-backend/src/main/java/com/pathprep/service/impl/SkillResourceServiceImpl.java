package com.pathprep.service.impl;

import com.pathprep.config.GroqProperties;
import com.pathprep.dto.SkillResourceRequest;
import com.pathprep.dto.response.SkillResourceResponse;
import com.pathprep.exception.ResourceNotFoundException;
import com.pathprep.model.SkillResource;
import com.pathprep.repository.SkillResourceRepository;
import com.pathprep.service.GroqAIService;
import com.pathprep.service.SkillResourceService;
import com.pathprep.util.ModelMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

    private static final Duration DATABASE_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration AI_GENERATION_TIMEOUT = Duration.ofSeconds(30);

    @Override
    @Cacheable(value = "skillResources", key = "#request.skillName + '_' + #request.role + '_' + #request.experienceLevel")
    public Mono<SkillResourceResponse> getOrGenerateSkillResources(SkillResourceRequest request) {
        String cacheKey = String.format("%s_%s_%s", 
            request.getSkillName(), request.getRole(), request.getExperienceLevel());
        log.info("Generating or retrieving skill resources for key: {}", cacheKey);
        
        return skillResourceRepository
            .findBySkillNameAndRoleAndExperienceLevel(
                request.getSkillName(), 
                request.getRole(), 
                request.getExperienceLevel())
            .timeout(DATABASE_TIMEOUT)
            .onErrorResume(IncorrectResultSizeDataAccessException.class, e -> {
                log.warn("Multiple skill resources found for key: {}. Using the most recent one.", cacheKey);
                return skillResourceRepository
                    .findBySkillNameAndRoleAndExperienceLevel(
                        request.getSkillName(), 
                        request.getRole(), 
                        request.getExperienceLevel())
                    .take(Duration.ofDays(1))
                    .timeout(DATABASE_TIMEOUT);
            })
            .switchIfEmpty(Mono.defer(() -> {
                log.info("No existing skill resources found for key: {}. Generating new one...", cacheKey);
                return generateSkillResourcesWithAI(request)
                    .timeout(AI_GENERATION_TIMEOUT);
            }))
            .flatMap(dbResource -> {
                if (shouldUpdateWithAI(dbResource)) {
                    log.info("Updating existing skill resources with AI for key: {}", cacheKey);
                    return generateSkillResourcesWithAI(request)
                        .timeout(AI_GENERATION_TIMEOUT)
                        .flatMap(aiResource -> combineResources(dbResource, aiResource));
                }
                log.info("Using existing skill resources from database for key: {}", cacheKey);
                return Mono.just(dbResource);
            })
            .map(this::convertToResponse)
            .onErrorResume(e -> {
                log.error("Error generating/retrieving skill resources for key: " + cacheKey, e);
                return Mono.error(new RuntimeException("Failed to generate or retrieve skill resources", e));
            });
    }

    @Override
    @Cacheable(value = "skillResources", key = "#id")
    public Mono<SkillResourceResponse> getSkillResourcesById(String id) {
        return skillResourceRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Skill resources not found with id: " + id)))
            .map(this::convertToResponse);
    }

    @Override
    @CacheEvict(value = "skillResources", key = "#id")
    public Mono<Void> deleteSkillResources(String id) {
        return skillResourceRepository.deleteById(id);
    }

    @Override
    @CacheEvict(value = "skillResources", key = "#id")
    public Mono<SkillResourceResponse> refreshSkillResources(String id) {
        return skillResourceRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Skill resources not found with id: " + id)))
            .flatMap(existing -> {
                SkillResourceRequest request = new SkillResourceRequest();
                request.setSkillName(existing.getSkillName());
                request.setRole(existing.getRole());
                request.setExperienceLevel(existing.getExperienceLevel());
                return generateSkillResourcesWithAI(request)
                    .flatMap(updated -> {
                        updated.setId(existing.getId());
                        updated.setCreatedAt(existing.getCreatedAt());
                        return skillResourceRepository.save(updated);
                    });
            })
            .map(this::convertToResponse);
    }

    private Mono<SkillResource> generateSkillResourcesWithAI(SkillResourceRequest request) {
        log.info("Generating new skill resources with AI for skill: {}, role: {}, level: {}", 
            request.getSkillName(), request.getRole(), request.getExperienceLevel());
        
        String prompt = buildPrompt(request);
        String model = groqProperties.getDefaultModel();
        
        return Mono.defer(() -> groqAIService.generateStructuredResponse(prompt, model, SkillResource.class))
            .flatMap(skillResource -> {
                // Set additional fields
                skillResource.setId(UUID.randomUUID().toString());
                skillResource.setSkillName(request.getSkillName());
                skillResource.setRole(request.getRole());
                skillResource.setExperienceLevel(request.getExperienceLevel());
                skillResource.setCreatedAt(LocalDateTime.now());
                skillResource.setUpdatedAt(LocalDateTime.now());
                
                return skillResourceRepository.save(skillResource)
                    .timeout(DATABASE_TIMEOUT);
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
