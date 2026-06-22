package com.wip.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Spring MVC controller that serves dedicated HTTP error pages for the Courier Tracking System.
 *
 * <p>This controller handles explicit error-related routes within the application, such as
 * the access-denied page shown when a user attempts to perform an action they are not
 * authorised to carry out. Error pages rendered here are full Thymeleaf HTML responses
 * rather than JSON, making them suitable for browser-facing interactions.</p>
 *
 * <p>Spring Security is configured to forward unauthorised access attempts to
 * {@code /access-denied}, which this controller handles by returning the
 * {@code error/access-denied} Thymeleaf template.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Controller
public class ErrorPageController {

    /**
     * Handles GET requests to {@code /access-denied} and renders the access-denied error page.
     *
     * <p>This endpoint is typically reached when Spring Security's access-control
     * mechanism determines that the authenticated user does not have the required
     * authority to access a protected resource. The rendered template displays a
     * user-friendly message explaining the restriction.</p>
     *
     * @return the logical view name {@code "error/access-denied"}
     */
    @GetMapping("/access-denied")
    public String accessDeniedPage() {
        return "error/access-denied";
    }
}
