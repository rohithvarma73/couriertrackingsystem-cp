package com.wip.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralised exception handler for all REST API controllers annotated with {@link RestController}.
 *
 * <p>This class is annotated with {@code @RestControllerAdvice} scoped exclusively to
 * {@code @RestController} beans, ensuring that its handlers only intercept exceptions
 * thrown within REST endpoints. UI controllers annotated with {@code @Controller} are
 * handled separately by {@code UiExceptionHandler}, which renders Thymeleaf HTML error
 * pages instead of JSON payloads.</p>
 *
 * <p>All handler methods return a {@link ResponseEntity} wrapping a structured
 * {@link Map} body that contains the following fields:</p>
 * <ul>
 *   <li>{@code timestamp} — the date and time the error occurred</li>
 *   <li>{@code status} — the numeric HTTP status code</li>
 *   <li>{@code error} — a short human-readable error type label</li>
 *   <li>{@code message} — a descriptive message explaining the cause</li>
 *   <li>{@code fieldErrors} — (validation errors only) a map of field names to their
 *       respective constraint violation messages</li>
 * </ul>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    /**
     * Handles {@link ResourceNotFoundException} thrown when a requested entity cannot be found.
     *
     * <p>Returns an HTTP {@code 404 Not Found} response with a JSON body containing the
     * exception message so that REST clients can display a meaningful error to the end user.</p>
     *
     * @param ex the {@link ResourceNotFoundException} that was thrown by a service or controller
     * @return a {@link ResponseEntity} with HTTP status {@code 404} and a JSON error body
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.NOT_FOUND.value());
        error.put("error", "Not Found");
        error.put("message", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link MethodArgumentNotValidException} thrown when a request body fails
     * Bean Validation constraints.
     *
     * <p>Iterates over all field-level constraint violations collected in the binding result
     * and builds a {@code fieldErrors} map keyed by field name. Returns an HTTP
     * {@code 400 Bad Request} response so the REST client can highlight the exact
     * form fields that are invalid.</p>
     *
     * @param ex the {@link MethodArgumentNotValidException} containing the full binding
     *           result with all constraint violations
     * @return a {@link ResponseEntity} with HTTP status {@code 400} and a JSON error body
     *         that includes a {@code fieldErrors} map of {@code fieldName → errorMessage}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(fieldName, message);
        });

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Validation Failed");
        error.put("message", "Invalid request data");
        error.put("fieldErrors", fieldErrors);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link IllegalStateException} thrown when a business-rule violation is detected.
     *
     * <p>Common scenarios include attempting to create a duplicate record or performing
     * a state transition that is not permitted (e.g., closing an already-closed shipment).
     * Returns an HTTP {@code 400 Bad Request} response.</p>
     *
     * @param ex the {@link IllegalStateException} describing the invalid application state
     * @return a {@link ResponseEntity} with HTTP status {@code 400} and a JSON error body
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles any unrecognised {@link Exception} not matched by a more specific handler.
     *
     * <p>Acts as a catch-all to prevent raw stack traces from leaking to REST clients.
     * The exception is printed to standard error for server-side diagnosis, while the
     * client receives a generic {@code 500 Internal Server Error} response with a safe,
     * non-revealing message.</p>
     *
     * @param ex the unexpected {@link Exception} that was not handled by a more specific handler
     * @return a {@link ResponseEntity} with HTTP status {@code 500} and a generic JSON error body
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        ex.printStackTrace();

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Internal Server Error");
        error.put("message", "Something went wrong");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}