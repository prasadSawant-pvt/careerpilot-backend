package com.pathprep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is an error while interacting with the AI service.
 * This exception is mapped to a 500 Internal Server Error HTTP response.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AIServiceException extends RuntimeException {

    public AIServiceException(String message) {
        super(message);
    }

    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AIServiceException(Throwable cause) {
        super(cause);
    }
}
