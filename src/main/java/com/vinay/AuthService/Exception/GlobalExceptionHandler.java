package com.vinay.AuthService.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    // ControllerAdvice makes this class a centralized exception handler for all controllers.
    // Methods annotated with @ExceptionHandler handle exceptions thrown from controller methods
    // and convert them into consistent HTTP JSON responses.

    /**
     * Handle common runtime exceptions thrown by application code (e.g. validation or
     * business-rule failures). We map them to 400 Bad Request and return a small JSON body
     * containing a timestamp, the exception message, and the numeric HTTP status.
     *
     * @param ex the RuntimeException that was thrown
     * @return a ResponseEntity with JSON body and 400 status
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex){
        // Build a concise JSON body rather than returning a full stack trace.
        Map<String, Object> body = Map.of(
                // ISO-like timestamp string for when the error occurred
                "timestamp", LocalDateTime.now().toString(),
                // A short human-readable message (comes from the exception)
                "message", ex.getMessage(),
                // HTTP status code for the response
                "status", HttpStatus.BAD_REQUEST.value()
        );

        // Return a 400 Bad Request with the constructed JSON error body.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Catch-all handler for any unanticipated exceptions. This prevents leaking stack traces
     * to the client and returns a generic 500 Internal Server Error response.
     *
     * Note: specific exceptions (for example security or persistence exceptions) can have
     * their own handlers for finer-grained control.
     *
     * @param ex the exception that was not otherwise handled
     * @return a ResponseEntity with JSON body and 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex){
        Map<String, Object> body = Map.of(
                // When debugging, consider returning a correlation id here to find logs.
                "timestamp", LocalDateTime.now().toString(),
                // Generic message to avoid leaking internal details to clients
                "message", "Internal server error",
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        // Return 500 Internal Server Error with a minimal JSON body.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

