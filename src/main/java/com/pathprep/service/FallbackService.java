package com.pathprep.service;

import reactor.core.publisher.Mono;

/**
 * Service interface for handling fallback operations when primary services fail.
 */
public interface FallbackService {
    
    /**
     * Handles fallback for database operations with a default value.
     * 
     * @param <T> The type of the response
     * @param error The exception that occurred
     * @param fallbackValue The value to return in case of failure
     * @return A Mono containing the fallback value
     */
    <T> Mono<T> handleDatabaseError(Throwable error, T fallbackValue);
    
    /**
     * Handles fallback for database operations with a custom error message.
     * 
     * @param <T> The type of the response
     * @param error The exception that occurred
     * @param errorMessage The error message to include in the exception
     * @return A Mono that errors with a ServiceUnavailableException
     */
    <T> Mono<T> handleDatabaseError(Throwable error, String errorMessage);
    
    /**
     * Handles fallback for database operations with a default value and operation context.
     * 
     * @param <T> The type of the response
     * @param throwable The exception that occurred
     * @param fallbackResponse The value to return in case of failure
     * @param operation The operation that failed
     * @return A Mono containing the fallback value
     */
    <T> Mono<T> handleDatabaseError(Throwable throwable, T fallbackResponse, String operation);
    
    /**
     * Logs the error and returns a default value.
     * 
     * @param <T> The type of the response
     * @param error The exception that occurred
     * @param fallbackValue The value to return
     * @param context Additional context about the operation
     * @return A Mono containing the fallback value
     */
    <T> Mono<T> handleErrorWithDefault(Throwable error, T fallbackValue, String context);
}
