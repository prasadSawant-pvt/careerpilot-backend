package com.pathprep.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a service is temporarily unavailable.
 */
@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
