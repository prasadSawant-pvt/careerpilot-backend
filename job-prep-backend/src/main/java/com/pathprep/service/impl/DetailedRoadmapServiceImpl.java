package com.pathprep.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathprep.config.GroqProperties;
import com.pathprep.dto.DetailedRoadmapRequest;
import com.pathprep.dto.response.DetailedRoadmapResponse;
import com.pathprep.exception.AIServiceException;
import com.pathprep.model.RoadmapPhase;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import java.time.Duration;
import java.time.LocalDateTime;
import com.pathprep.exception.ResourceNotFoundException;
import com.pathprep.model.DetailedRoadmap;
import com.pathprep.repository.DetailedRoadmapRepository;
import com.pathprep.service.DetailedRoadmapService;
import com.pathprep.service.GroqAIService;
import org.modelmapper.ModelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the DetailedRoadmapService interface.
 * Handles business logic for detailed roadmap generation and management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DetailedRoadmapServiceImpl implements DetailedRoadmapService {

    private final DetailedRoadmapRepository roadmapRepository;
    private final GroqAIService groqAIService;
    private final GroqProperties groqProperties;
    private final ModelMapper modelMapper;

    private int getDefaultTimeline(String experienceLevel) {
        if (experienceLevel == null) {
            return 12; // Default to intermediate
        }
        return switch (experienceLevel.toLowerCase()) {
            case "beginner" -> 16;
            case "advanced" -> 8;
            default -> 12; // INTERMEDIATE
        };
    }
    
    @Override
    @Cacheable(value = "roadmaps", key = "#request.compositeKey")
    public Mono<DetailedRoadmapResponse> generateOrGetRoadmap(DetailedRoadmapRequest request) {
        // Set default timeline if not provided
        if (request.getTimelineWeeks() == null) {
            request.setTimelineWeeks(getDefaultTimeline(request.getExperienceLevel()));
        }
        String compositeKey = request.getCompositeKey();
        log.info("Generating or retrieving roadmap for key: {}", compositeKey);
        
        // First try to get from database
        return roadmapRepository.findByCompositeKey(compositeKey)
            .onErrorResume(IncorrectResultSizeDataAccessException.class, e -> {
                log.warn("Multiple roadmaps found for key: {}. Using the most recent one.", compositeKey);
                // The repository should have already handled getting the most recent one
                return Mono.empty();
            })
            .switchIfEmpty(Mono.defer(() -> {
                log.info("No existing roadmap found for key: {}. Generating new one...", compositeKey);
                return generateRoadmapWithAI(request);
            }))
            .flatMap(dbRoadmap -> {
                // If we have a DB roadmap, check if we should update it with AI data
                if (shouldUpdateWithAI(dbRoadmap)) {
                    log.info("Updating existing roadmap with AI data for key: {}", compositeKey);
                    return generateRoadmapWithAI(request)
                        .flatMap(aiRoadmap -> combineRoadmaps(dbRoadmap, aiRoadmap));
                }
                log.info("Using existing roadmap from database for key: {}", compositeKey);
                return Mono.just(dbRoadmap);
            })
            .map(this::convertToResponse)
            .onErrorResume(e -> {
                log.error("Error generating/retrieving roadmap for key: " + compositeKey, e);
                return Mono.error(new RuntimeException("Failed to generate or retrieve roadmap", e));
            });
    }
    
    /**
     * Combines data from database and AI-generated roadmaps, removing duplicates
     */
    @Override
    public Mono<DetailedRoadmap> generateRoadmapWithAI(DetailedRoadmapRequest request) {
        log.info("Generating new roadmap with AI for role: {}, level: {}", 
                request.getRole(), request.getExperienceLevel());
        
        String prompt = buildPrompt(request);
        String model = groqProperties.getDefaultModel();
        
        return groqAIService.generateStructuredResponse(prompt, model, DetailedRoadmap.class)
                .flatMap(roadmap -> {
                    // Set additional fields
                    roadmap.setId(UUID.randomUUID().toString());
                    roadmap.setCompositeKey(request.getCompositeKey());
                    roadmap.setRole(request.getRole());
                    roadmap.setExperienceLevel(request.getExperienceLevel());
                    roadmap.setCreatedAt(LocalDateTime.now());
                    roadmap.setUpdatedAt(LocalDateTime.now());
                    
                    // Calculate total estimated weeks
                    int totalWeeks = roadmap.getPhases() != null ? 
                            roadmap.getPhases().size() : 0;
                    roadmap.setEstimatedWeeks(totalWeeks);
                    
                    return saveRoadmap(roadmap);
                });
    }
    
    private Mono<DetailedRoadmap> combineRoadmaps(DetailedRoadmap dbRoadmap, DetailedRoadmap aiRoadmap) {
        // Create a new roadmap with combined data
        DetailedRoadmap combined = new DetailedRoadmap();
        
        // Use DB data as base
        combined.setId(dbRoadmap.getId());
        combined.setCompositeKey(dbRoadmap.getCompositeKey());
        combined.setRole(dbRoadmap.getRole());
        combined.setExperienceLevel(dbRoadmap.getExperienceLevel());
        combined.setCreatedAt(dbRoadmap.getCreatedAt());
        combined.setUpdatedAt(LocalDateTime.now());
        
        // Combine phases from both sources, removing duplicates
        List<RoadmapPhase> combinedPhases = new ArrayList<>();
        Set<String> phaseNames = new HashSet<>();
        
        // Add DB phases first
        if (dbRoadmap.getPhases() != null) {
            for (RoadmapPhase phase : dbRoadmap.getPhases()) {
                if (phase != null && phase.getPhaseName() != null && !phaseNames.contains(phase.getPhaseName())) {
                    combinedPhases.add(phase);
                    phaseNames.add(phase.getPhaseName());
                }
            }
        }
        
        // Add AI phases that don't exist in DB
        if (aiRoadmap.getPhases() != null) {
            for (RoadmapPhase phase : aiRoadmap.getPhases()) {
                if (phase != null && phase.getPhaseName() != null && !phaseNames.contains(phase.getPhaseName())) {
                    combinedPhases.add(phase);
                    phaseNames.add(phase.getPhaseName());
                }
            }
        }
        
        // Sort phases by week number
        combinedPhases.sort(Comparator.comparingInt(RoadmapPhase::getWeekNumber));
        
        // Set the combined phases directly
        combined.setPhases(combinedPhases);
        
        // Update estimated weeks
        if (!combinedPhases.isEmpty()) {
            combined.setEstimatedWeeks(combinedPhases.stream()
                .mapToInt(p -> p.getWeekNumber() != null ? p.getWeekNumber() : 1)
                .max()
                .orElse(1));
        } else {
            combined.setEstimatedWeeks(dbRoadmap.getEstimatedWeeks() != null ? 
                dbRoadmap.getEstimatedWeeks() : 
                (aiRoadmap.getEstimatedWeeks() != null ? aiRoadmap.getEstimatedWeeks() : 1));
        }
        
        // Save the combined roadmap
        return saveRoadmap(combined);
    }

    @Override
    @Cacheable(value = "roadmaps", key = "#compositeKey")
    public Mono<DetailedRoadmapResponse> getRoadmapByCompositeKey(String compositeKey) {
        log.debug("Retrieving roadmap with key: {}", compositeKey);
        return roadmapRepository.findByCompositeKey(compositeKey)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Roadmap not found with key: " + compositeKey)))
                .map(this::convertToResponse);
    }

    @Override
    @CacheEvict(value = "roadmaps", key = "#roadmap.compositeKey")
    public Mono<DetailedRoadmap> saveRoadmap(DetailedRoadmap roadmap) {
        log.info("Saving roadmap with key: {}", roadmap.getCompositeKey());
        if (roadmap.getId() == null) {
            roadmap.setId(UUID.randomUUID().toString());
            roadmap.setCreatedAt(LocalDateTime.now());
        }
        roadmap.setUpdatedAt(LocalDateTime.now());
        return roadmapRepository.save(roadmap)
            .doOnSuccess(saved -> log.info("Successfully saved roadmap with key: {}", saved.getCompositeKey()))
            .doOnError(e -> log.error("Error saving roadmap with key: " + roadmap.getCompositeKey(), e));
    }

    @Override
    @CacheEvict(value = "roadmaps", key = "#compositeKey")
    public Mono<Void> deleteRoadmap(String id) {
        log.debug("Deleting roadmap with id: {}", id);
        return roadmapRepository.deleteById(id);
    }

    /**
     * Determines if an existing roadmap should be updated with AI data
     */
    private boolean shouldUpdateWithAI(DetailedRoadmap roadmap) {
        // Update if the roadmap is older than 30 days
        if (roadmap.getUpdatedAt() != null) {
            return Duration.between(roadmap.getUpdatedAt(), LocalDateTime.now()).toDays() > 30;
        }
        // If no update timestamp, update it
        return true;
    }
    
    private String buildPrompt(DetailedRoadmapRequest request) {
    // Ensure we have a valid timeline
    int weeks = request.getTimelineWeeks() != null ? 
        request.getTimelineWeeks() : 
        getDefaultTimeline(request.getExperienceLevel());
        
    return String.format("""
        Generate a detailed learning roadmap for a %s %s.
        
        IMPORTANT: Your response must be a valid JSON object without any markdown formatting, extra text, or code blocks.
        Do not include any explanations or notes outside the JSON structure.
        
        The JSON must follow this exact structure:
        {
          "phases": [
            {
              "phaseName": "string",
              "weekNumber": number,
              "objective": "string",
              "topics": [
                {
                  "topicName": "string",
                  "description": "string",
                  "estimatedHours": number,
                  "difficulty": "string",
                  "subtopics": [
                    {
                      "name": "string",
                      "description": "string"
                    }
                  ]
                }
              ],
              "deliverables": ["string"]
            }
          ]
        }
        
        Guidelines:
        - Create a %d-week learning plan
        - For each phase, include 3-5 topics
        - Each topic must be a complete object with all required fields
        - Each topic must include topicName, description, estimatedHours, and difficulty
        - Include 2-3 subtopics for each main topic
        - Set appropriate difficulty levels (Beginner/Intermediate/Advanced)
        - Include 2-3 deliverables per phase
        - Focus on practical, hands-on learning
        - Include both theoretical concepts and practical applications
        - Structure the content to build upon previous knowledge
        - Include relevant technologies and tools
        
        Role: %s
        Experience Level: %s
        Current Skills: %s
        Focus Area: %s
        
        Important Notes:
        1. The response must be valid JSON that can be parsed by Jackson
        2. Do not include any markdown formatting (no ```json or ```)
        3. Do not include any explanatory text outside the JSON structure
        4. Ensure all strings are properly escaped
        5. Include all required fields in the JSON structure
        6. The response must be a single, valid JSON object
        7. The topics array must contain objects, not strings
        8. Each topic must have at minimum: topicName, description, estimatedHours, difficulty
        9. estimatedHours should be a number between 1-40 for each topic
        10. difficulty must be one of: Beginner, Intermediate, Advanced
        """,
        request.getExperienceLevel(),
        request.getRole(),
        weeks,
        request.getRole(),
        request.getExperienceLevel(),
        request.getCurrentSkills() != null && !request.getCurrentSkills().isEmpty() ? 
            String.join(", ", request.getCurrentSkills()) : "None specified",
        request.getFocusArea() != null ? request.getFocusArea() : "General"
    );
}

    private DetailedRoadmapResponse convertToResponse(DetailedRoadmap roadmap) {
        if (roadmap == null) {
            return null;
        }
        return modelMapper.map(roadmap, DetailedRoadmapResponse.class);
    }
}
