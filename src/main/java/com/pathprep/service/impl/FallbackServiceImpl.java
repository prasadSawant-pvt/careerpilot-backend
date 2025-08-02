package com.pathprep.service.impl;

import com.pathprep.exception.ServiceUnavailableException;
import com.pathprep.service.FallbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class FallbackServiceImpl implements FallbackService {

    @Override
    public <T> Mono<T> handleDatabaseError(Throwable error, T fallbackValue) {
        logError(error, "Database operation failed, using fallback value");
        return Mono.just(fallbackValue);
    }

    @Override
    public <T> Mono<T> handleDatabaseError(Throwable error, String errorMessage) {
        logError(error, errorMessage);
        return Mono.error(new ServiceUnavailableException("Service temporarily unavailable. Please try again later."));
    }

    @Override
    public <T> Mono<T> handleDatabaseError(Throwable throwable, T fallbackResponse, String operation) {
        logError(throwable, operation);
        return Mono.just(fallbackResponse);
    }

    @Override
    public <T> Mono<T> handleErrorWithDefault(Throwable error, T fallbackValue, String context) {
        logError(error, "Error in " + context + ", using fallback value");
        return Mono.just(fallbackValue);
    }

    private void logError(Throwable error, String message) {
        if (isConnectionError(error)) {
            log.error("Database connection error: {}. {}", message, error.getMessage());
        } else if (isTimeoutError(error)) {
            log.error("Database timeout: {}. {}", message, error.getMessage());
        } else {
            log.error("{} Error: {}", message, error.getMessage(), error);
        }
    }

    private boolean isConnectionError(Throwable error) {
        return error instanceof DataAccessResourceFailureException ||
                error.getCause() instanceof DataAccessResourceFailureException ||
                (error.getMessage() != null && error.getMessage().contains("Connection"));
    }

    private boolean isTimeoutError(Throwable error) {
        return error instanceof TimeoutException ||
                error.getCause() instanceof TimeoutException ||
                error instanceof SocketTimeoutException ||
                (error.getMessage() != null && error.getMessage().contains("timed out"));
    }
}
