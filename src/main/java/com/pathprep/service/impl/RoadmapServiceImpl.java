package com.pathprep.service.impl;

import com.pathprep.model.Roadmap;
import com.pathprep.repository.RoadmapRepository;
import com.pathprep.service.GroqService;
import com.pathprep.service.RoadmapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of the RoadmapService interface for generating and managing learning roadmaps.
 * Uses reactive programming with Project Reactor (Mono/Flux) for non-blocking operations.
 */
@Service
public class RoadmapServiceImpl implements RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final GroqService groqService;
    public static final Logger log = LoggerFactory.getLogger(RoadmapServiceImpl.class);

    public RoadmapServiceImpl(RoadmapRepository roadmapRepository, GroqService groqService) {
        this.roadmapRepository = roadmapRepository;
        this.groqService = groqService;
    }

    @Override
    public Mono<Roadmap> generateRoadmap(String role, String experience, List<String> skills) {
        log.debug("Generating roadmap for role: {}, experience: {}, skills: {}", role, experience, skills);
        
        // First check if a roadmap already exists
        return roadmapRepository.findByRoleAndExperience(role, experience)
                .switchIfEmpty(Mono.defer(() -> {
                    // If not found, generate a new one
                    log.debug("No existing roadmap found, generating new one");
                    return generateNewRoadmap(role, experience, skills);
                }))
                .onErrorResume(e -> {
                    log.error("Error in generateRoadmap: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("Failed to generate roadmap: " + e.getMessage(), e));
                });
    }
    
    @Override
    public Mono<Roadmap> getRoadmap(String id) {
        log.debug("Fetching roadmap with id: {}", id);
        return roadmapRepository.findById(id);
    }
    
    @Override
    public Mono<String> queryGroqModel(String prompt, String model) {
        log.debug("Querying Groq model with prompt: {}", prompt);
        String modelToUse = (model != null && !model.trim().isEmpty()) ? model : "llama3-8b-8192";
        
        return groqService.generateText(prompt)
                .onErrorResume(e -> {
                    log.error("Error querying Groq model: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("Failed to query Groq model: " + e.getMessage(), e));
                });
    }
    
    @Override
    public Flux<Roadmap> getRecentRoadmaps(int limit) {
        log.debug("Fetching {} most recent roadmaps", limit);
        return roadmapRepository.findAllByOrderByCreatedAtDesc()
                .take(limit);
    }
    
    @Override
    public Flux<Roadmap> getTrendingRoadmaps(int limit) {
        log.debug("Fetching {} most popular roadmaps", limit);
        return roadmapRepository.findAllByOrderByPopularityDesc()
                .take(limit);
    }
    
    @Override
    public Mono<Roadmap> saveRoadmap(Roadmap roadmap) {
        log.debug("Saving roadmap: {}", roadmap);
        return roadmapRepository.save(roadmap);
    }
    
    @Override
    public Mono<Void> deleteRoadmap(String id) {
        log.debug("Deleting roadmap with id: {}", id);
        return roadmapRepository.deleteById(id);
    }
    
    /**
     * Generates a new roadmap using Groq AI based on the provided role, experience, and skills.
     * 
     * @param role The target role for the roadmap
     * @param experience The experience level for the roadmap
     * @param skills List of skills to include in the roadmap
     * @return A Mono containing the newly generated Roadmap
     */
    private Mono<Roadmap> generateNewRoadmap(String role, String experience, List<String> skills) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Create a detailed learning roadmap for a ").append(experience)
                .append(" level ").append(role).append(".\n\n");
        if (skills != null && !skills.isEmpty()) {
            prompt.append("Focus on the following skills: ").append(String.join(", ", skills)).append("\n\n");
        }
        prompt.append("The roadmap should include:\n")
                .append("1. Core concepts to master\n")
                .append("2. Learning resources (free and paid)\n")
                .append("3. Practical projects to build\n")
                .append("4. Common interview questions\n")
                .append("5. Best practices and patterns\n")
                .append("6. Community resources (blogs, forums, conferences)\n")
                .append("Format the response in Markdown with clear sections.\n");

        return groqService.generateText(prompt.toString())
                .flatMap(aiResponse -> {
                    Roadmap roadmap = new Roadmap();
                    roadmap.setRole(role);
                    roadmap.setExperience(experience);
                    roadmap.setSkills(skills);
                    roadmap.setTimeline("3-6 months");
                    roadmap.setCreatedAt(LocalDateTime.now());
                    roadmap.setPopularity(0);
                    // Optionally: parse aiResponse and map to roadmap sections
                    log.debug("AI Response: {}", aiResponse);
                    return roadmapRepository.save(roadmap);
                });
    }
    
    @Override
    public Mono<Roadmap> getOrGenerateRoadmap(String role, String experience) {
        log.debug("Getting or generating roadmap for role: {}, experience: {}", role, experience);
        
        // First try to find an existing roadmap
        return roadmapRepository.findByRoleAndExperience(role, experience)
                .switchIfEmpty(Mono.defer(() -> {
                    // If not found, generate a new one with empty skills list
                    log.debug("No existing roadmap found, generating new one for role: {}, experience: {}", role, experience);
                    return generateNewRoadmap(role, experience, List.of());
                }))
                .onErrorResume(e -> {
                    log.error("Error in getOrGenerateRoadmap: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("Failed to get or generate roadmap: " + e.getMessage(), e));
                });
    }
}
