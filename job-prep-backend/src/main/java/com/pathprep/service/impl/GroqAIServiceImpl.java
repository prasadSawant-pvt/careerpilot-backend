package com.pathprep.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathprep.config.GroqProperties;
import com.pathprep.exception.AIServiceException;
import com.pathprep.model.*;
import com.pathprep.dto.InterviewQuestionResponse;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.pathprep.service.GroqAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Implementation of GroqAIService for interacting with the Groq AI API.
 * Handles sending prompts and processing responses from the AI model.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroqAIServiceImpl implements GroqAIService {

    private final WebClient groqWebClient;
    private final GroqProperties groqProperties;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<String> generateText(String prompt, String model) {
        log.debug("Sending text generation request to Groq AI");
        return groqWebClient
                .post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createChatRequest(prompt, model))
                .retrieve()
                .bodyToMono(GroqChatResponse.class)
                .map(response -> {
                    if (response.getChoices() == null || response.getChoices().isEmpty()) {
                        throw new AIServiceException("No response from AI model");
                    }
                    return response.getChoices().get(0).getMessage().getContent();
                })
                .onErrorMap(e -> {
                    log.error("Error generating text with Groq AI: {}", e.getMessage(), e);
                    return new AIServiceException("Failed to generate text: " + e.getMessage(), e);
                });
    }

    @Override
    public <T> Mono<T> generateStructuredResponse(String prompt, String model, Class<T> responseType) {
        log.debug("Generating structured response for type: {}", responseType.getSimpleName());
        return generateText(prompt, model)
                .flatMap(response -> {
                    try {
                        // Clean the response to ensure it's valid JSON
                        String jsonResponse = cleanJsonResponse(response);
                        log.debug("Attempting to parse JSON: {}", jsonResponse);
                        
                        // First try to parse as is
                        try {
                            // Check if we're expecting a DetailedRoadmap and the response is an array
                            if (responseType.equals(DetailedRoadmap.class) && jsonResponse.trim().startsWith("[")) {
                                log.debug("Detected array response for DetailedRoadmap, converting to object with phases");
                                // Parse the array of phases
                                List<Map<String, Object>> phases = objectMapper.readValue(jsonResponse,
                                    new TypeReference<List<Map<String, Object>>>() {});
                                
                                // Create a new DetailedRoadmap with the phases
                                DetailedRoadmap roadmap = new DetailedRoadmap();
                                // Extract role and experience level from the prompt if possible
                                String role = extractValueFromPrompt(prompt, "role");
                                String experienceLevel = extractValueFromPrompt(prompt, "experienceLevel");
                                
                                roadmap.setRole(role);
                                roadmap.setExperienceLevel(experienceLevel);
                                roadmap.setCompositeKey();
                                roadmap.setPhases(phases);
                                roadmap.setCreatedAt(LocalDateTime.now());
                                roadmap.setUpdatedAt(LocalDateTime.now());
                                
                                return Mono.just(responseType.cast(roadmap));
                            } 
                            // Handle SkillResource array response
                            else if (responseType.equals(SkillResource.class) && jsonResponse.trim().startsWith("[")) {
                                log.debug("Detected array response for SkillResource, converting to object with learningPaths");
                                // Parse the array of resource items
                                List<SkillResource.ResourceItem> resourceItems = objectMapper.readValue(jsonResponse,
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, SkillResource.ResourceItem.class));
                                
                                // Create a new SkillResource with the items in learningPaths
                                SkillResource skillResource = new SkillResource();
                                String skillName = extractValueFromPrompt(prompt, "skillName");
                                String role = extractValueFromPrompt(prompt, "role");
                                String experienceLevel = extractValueFromPrompt(prompt, "experienceLevel");
                                
                                skillResource.setSkillName(skillName);
                                skillResource.setRole(role);
                                skillResource.setExperienceLevel(experienceLevel);
                                skillResource.setLearningPaths(resourceItems);
                                skillResource.setCreatedAt(LocalDateTime.now());
                                skillResource.setUpdatedAt(LocalDateTime.now());
                                
                                return Mono.just(responseType.cast(skillResource));
                            }
                            // Handle InterviewQuestionResponse array response
                            else if (responseType.getSimpleName().equals("InterviewQuestionResponse") && jsonResponse.trim().startsWith("[")) {
                                log.debug("Detected array response for InterviewQuestionResponse, converting to object with questions array");
                                // Parse the array of questions
                                List<InterviewQuestionResponse.QuestionItem> questions = objectMapper.readValue(jsonResponse,
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, InterviewQuestionResponse.QuestionItem.class));
                                
                                // Create a new InterviewQuestionResponse with the questions
                                InterviewQuestionResponse questionResponse = new InterviewQuestionResponse();
                                String role = extractValueFromPrompt(prompt, "role");
                                String experienceLevel = extractValueFromPrompt(prompt, "experienceLevel");
                                
                                questionResponse.setRole(role);
                                questionResponse.setExperienceLevel(experienceLevel);
                                questionResponse.setQuestions(questions);
                                
                                return Mono.just(responseType.cast(questionResponse));
                            }
                            else {
                                // Normal case - parse as the expected type
                                T result = objectMapper.readValue(jsonResponse, responseType);
                                return Mono.just(result);
                            }
                        } catch (JsonProcessingException e) {
                            log.warn("Initial JSON parse failed, trying to fix common issues: {}", e.getMessage());
                            
                            // If parsing fails, try to fix common JSON issues
                            try {
                                // Try to parse as a JSON string that might be wrapped in quotes
                                if (jsonResponse.startsWith("\"") && jsonResponse.endsWith("\"")) {
                                    jsonResponse = jsonResponse.substring(1, jsonResponse.length() - 1)
                                            .replace("\\\"", "\"");
                                }
                                
                                // Try to parse the cleaned response again
                                T result = objectMapper.readValue(jsonResponse, responseType);
                                return Mono.just(result);
                            } catch (JsonProcessingException ex) {
                                log.error("Failed to parse AI response after cleaning. Response: {}", jsonResponse, ex);
                                return Mono.error(new AIServiceException("Failed to parse AI response after cleaning: " + 
                                    ex.getMessage() + "\nResponse: " + jsonResponse, ex));
                            }
                        }
                    } catch (Exception e) {
                        log.error("Unexpected error processing AI response", e);
                        return Mono.error(new AIServiceException("Unexpected error processing AI response: " + e.getMessage(), e));
                    }
                });
    }

    /**
     * Extracts a value from the prompt string by looking for a pattern like "key: value"
     * @param prompt The prompt string
     * @param key The key to look for
     * @return The extracted value or null if not found
     */
    private String extractValueFromPrompt(String prompt, String key) {
        if (prompt == null || key == null) {
            return null;
        }
        // Look for pattern like "role: Java Developer" or "experienceLevel: Beginner"
        String pattern = "(?i)\\b" + key + "\s*:\\s*([^\n\r]+)";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(prompt);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }
    
    @Override
    public <T> Mono<T> generateStructuredResponse(String prompt, String model, TypeReference<T> typeReference) {
        log.debug("Generating structured response for type: {}", typeReference.getType().getTypeName());
        return generateText(prompt, model)
                .flatMap(response -> {
                    try {
                        // Clean the response to ensure it's valid JSON
                        String jsonResponse = cleanJsonResponse(response);
                        T result = objectMapper.readValue(jsonResponse, typeReference);
                        return Mono.just(result);
                    } catch (JsonProcessingException e) {
                        log.error("Error parsing AI response: {}", e.getMessage());
                        return Mono.error(new AIServiceException("Failed to parse AI response", e));
                    }
                });
    }

    private GroqChatRequest createChatRequest(String prompt, String model) {
        String modelToUse = model != null ? model : groqProperties.getDefaultModel();
        GroqChatRequest request = new GroqChatRequest();
        request.setModel(modelToUse);
        
        GroqMessage message = new GroqMessage();
        message.setRole("user");
        message.setContent(prompt);
        
        request.setMessages(Collections.singletonList(message));
        request.setTemperature(0.7);
        request.setMaxTokens(4000);
        
        return request;
    }

    /**
     * Cleans the JSON response from the AI to ensure it's valid JSON.
     * Removes markdown code blocks and trims whitespace.
     */
    private String cleanJsonResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "{}";
        }
        
        // Remove markdown code blocks if present
        String cleaned = response.replaceAll("(?s)```(?:json)?\\s*", "")  // Remove opening ```json
                               .replaceAll("```\\s*$", "")                // Remove closing ```
                               .trim();
        
        // Remove any non-printable characters except newlines and tabs
        cleaned = cleaned.replaceAll("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F\\u007F-\\u009F]", "");
        
        // Fix common JSON issues
        cleaned = cleaned
            // Fix missing commas between objects in arrays
            .replaceAll("}\\s*\\{", "},{")
            // Fix missing quotes around field names
            .replaceAll("(?<!\")([a-zA-Z0-9_]+)(?=:)", "$1")
            // Fix single quotes around property names
            .replaceAll("([{,]\s*)'([^']+)'\s*:", "$1\"$2\":")
            // Fix single quotes around string values
            .replaceAll(":\s*'([^']+)'([,}])$", ": \"$1\"$2")
            .replaceAll(":\s*'([^']+)'([,}])[\s\r\n]*", ": \"$1\"$2\n");
        
        // Try to find JSON object or array in the response
        int jsonStart = Math.max(cleaned.indexOf('{'), cleaned.indexOf('['));
        int jsonEnd = Math.max(cleaned.lastIndexOf('}'), cleaned.lastIndexOf(']'));
        
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            cleaned = cleaned.substring(jsonStart, jsonEnd + 1);
        }
        
        log.debug("Cleaned JSON response: {}", cleaned);
        return cleaned;
    }
}
