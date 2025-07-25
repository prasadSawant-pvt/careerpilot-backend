package com.pathprep.service;

import com.fasterxml.jackson.core.type.TypeReference;
import reactor.core.publisher.Mono;

/**
 * Service for interacting with the Groq AI API.
 * Handles sending prompts and processing responses from the AI model.
 */
public interface GroqAIService {
    
    /**
     * Generate a text response from the AI model.
     * 
     * @param prompt The prompt to send to the AI
     * @param model The AI model to use (e.g., "llama3-8b-8192")
     * @return A Mono containing the generated text response
     */
    Mono<String> generateText(String prompt, String model);
    
    /**
     * Generate a structured response from the AI model.
     * 
     * @param <T> The type of the response object
     * @param prompt The prompt to send to the AI
     * @param model The AI model to use
     * @param responseType The class of the response object
     * @return A Mono containing the deserialized response object
     */
    <T> Mono<T> generateStructuredResponse(String prompt, String model, Class<T> responseType);
    
    /**
     * Generate a structured response from the AI model with a TypeReference.
     * Useful for complex generic types.
     * 
     * @param <T> The type of the response object
     * @param prompt The prompt to send to the AI
     * @param model The AI model to use
     * @param typeReference The TypeReference for the response type
     * @return A Mono containing the deserialized response object
     */
    <T> Mono<T> generateStructuredResponse(String prompt, String model, TypeReference<T> typeReference);
}
