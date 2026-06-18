package com.wip.exception;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Handles exceptions thrown by Thymeleaf UI @Controller classes.
 * Renders proper HTML error pages instead of JSON.
 *
 * Scoped to @Controller beans only — REST controllers use GlobalExceptionHandler.
 */
@ControllerAdvice(annotations = Controller.class)
public class UiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorTitle", "Record not found");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/not-found";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleBadRequest(IllegalStateException ex, Model model) {
        model.addAttribute("errorTitle", "Action not allowed");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/bad-request";
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public String handleAccessDenied(Exception ex, Model model) {
        model.addAttribute("errorTitle", "Access denied");
        model.addAttribute("errorMessage", "You don't have permission to perform this action.");
        return "error/access-denied";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        ex.printStackTrace();
        model.addAttribute("errorTitle", "Something went wrong");
        model.addAttribute("errorMessage",
            "An unexpected error occurred. Please try again or contact support if this continues.");
        return "error/general";
    }
}
