package com.wip.exception;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Centralised exception handler for Thymeleaf UI controllers annotated with {@link Controller}.
 *
 * <p>This class is annotated with {@code @ControllerAdvice} scoped exclusively to
 * {@code @Controller} beans. Rather than returning JSON error payloads, it renders
 * proper HTML error pages so that browser-facing users receive user-friendly error
 * information. REST controllers annotated with {@code @RestController} are handled
 * separately by {@code GlobalExceptionHandler}.</p>
 *
 * <p>Each handler method populates two standard model attributes before returning a view name:</p>
 * <ul>
 *   <li>{@code errorTitle} — a concise title summarising the nature of the error</li>
 *   <li>{@code errorMessage} — a descriptive message with actionable information for the user</li>
 * </ul>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@ControllerAdvice(annotations = Controller.class)
public class UiExceptionHandler {

    /**
     * Handles {@link ResourceNotFoundException} thrown by UI controllers when a requested
     * record cannot be found in the system.
     *
     * <p>Adds the exception message as the {@code errorMessage} model attribute and renders
     * the {@code error/not-found} Thymeleaf template with an HTTP {@code 200} response
     * (error status is communicated visually to the user rather than via HTTP headers).</p>
     *
     * @param ex    the {@link ResourceNotFoundException} carrying the "not found" detail message
     * @param model the Spring MVC {@link Model} used to pass error details to the view;
     *              populated with {@code errorTitle} and {@code errorMessage}
     * @return the logical view name {@code "error/not-found"}
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorTitle", "Record not found");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/not-found";
    }

    /**
     * Handles {@link IllegalStateException} thrown by UI controllers when a requested
     * action violates a business rule or is otherwise not permitted.
     *
     * <p>Common scenarios include attempting an operation on an entity that is in an
     * incompatible state. Renders the {@code error/bad-request} Thymeleaf template.</p>
     *
     * @param ex    the {@link IllegalStateException} describing the disallowed action
     * @param model the Spring MVC {@link Model} used to pass error details to the view;
     *              populated with {@code errorTitle} and {@code errorMessage}
     * @return the logical view name {@code "error/bad-request"}
     */
    @ExceptionHandler(IllegalStateException.class)
    public String handleBadRequest(IllegalStateException ex, Model model) {
        model.addAttribute("errorTitle", "Action not allowed");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/bad-request";
    }

    /**
     * Handles {@link org.springframework.security.access.AccessDeniedException} thrown when
     * the authenticated user attempts to access a resource or perform an action they are
     * not authorised to perform.
     *
     * <p>Renders the {@code error/access-denied} Thymeleaf template with a generic
     * permission-denied message so that sensitive internal details are not disclosed.</p>
     *
     * @param ex    the {@link Exception} representing the access-denial condition
     *              (typed as {@link Exception} to satisfy the Spring MVC handler signature)
     * @param model the Spring MVC {@link Model} used to pass error details to the view;
     *              populated with {@code errorTitle} and {@code errorMessage}
     * @return the logical view name {@code "error/access-denied"}
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public String handleAccessDenied(Exception ex, Model model) {
        model.addAttribute("errorTitle", "Access denied");
        model.addAttribute("errorMessage", "You don't have permission to perform this action.");
        return "error/access-denied";
    }

    /**
     * Handles any unrecognised {@link Exception} not matched by a more specific handler
     * within the UI controller layer.
     *
     * <p>Acts as a catch-all to prevent raw stack traces from being displayed to the user.
     * The exception is printed to standard error for server-side diagnosis. The
     * {@code error/general} Thymeleaf template is rendered with a safe, generic message
     * that advises the user to retry or contact support.</p>
     *
     * @param ex    the unexpected {@link Exception} that was not matched by a more
     *              specific handler
     * @param model the Spring MVC {@link Model} used to pass error details to the view;
     *              populated with {@code errorTitle} and {@code errorMessage}
     * @return the logical view name {@code "error/general"}
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        ex.printStackTrace();
        model.addAttribute("errorTitle", "Something went wrong");
        model.addAttribute("errorMessage",
            "An unexpected error occurred. Please try again or contact support if this continues.");
        return "error/general";
    }
}
