package com.wip.controller;

import com.wip.dto.RegisterDto;
import com.wip.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Spring MVC controller that manages user authentication and registration flows.
 *
 * <p>This controller handles the user-facing login and registration pages for the
 * Courier Tracking System. It exposes two routes under {@code /register}: a GET
 * endpoint that renders an empty registration form, and a POST endpoint that
 * validates and submits the form data to {@link AuthService} for account creation.
 * It also provides the {@code /login} route for Spring Security's login page.</p>
 *
 * <p>On successful registration the user is redirected to {@code /login?registered}
 * so the login page can display a confirmation message. Validation errors and
 * business-logic failures (e.g., duplicate username) are surfaced back to the form
 * via model attributes.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Controller
public class AuthController {

    private final AuthService authService;

    /**
     * Constructs an {@code AuthController} with the required authentication service.
     *
     * @param authService the service responsible for user registration and authentication logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles GET requests to {@code /login}.
     *
     * <p>Returns the Thymeleaf login view. Spring Security automatically processes
     * the form submission; this method only serves the page.</p>
     *
     * @return the logical view name {@code "login"}
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * Handles GET requests to {@code /register} and displays the user registration form.
     *
     * <p>An empty {@link RegisterDto} is added to the model so that the Thymeleaf
     * template can bind the form fields via {@code th:object}.</p>
     *
     * @param model the Spring MVC {@link Model} used to pass the empty form DTO to the view
     * @return the logical view name {@code "register"}
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "register";
    }

    /**
     * Handles POST requests to {@code /register} and processes the user registration form.
     *
     * <p>The submitted {@link RegisterDto} is validated using Bean Validation constraints.
     * If any constraint violations are found, the registration form is re-rendered with
     * the binding errors intact. If validation passes, the data is forwarded to
     * {@link AuthService#register(RegisterDto)}. A {@link RuntimeException} thrown by
     * the service (e.g., because the username or e-mail is already taken) is caught and
     * its message is exposed to the view via the {@code errorMessage} model attribute.</p>
     *
     * @param registerDto   the form-backed DTO populated from the HTTP request body;
     *                      validated via {@code @Valid}
     * @param bindingResult holds any Bean Validation errors detected for {@code registerDto}
     * @param model         the Spring MVC {@link Model} used to pass error messages to the view
     * @return {@code "redirect:/login?registered"} on success; {@code "register"} view
     *         if validation fails or the service throws a {@link RuntimeException}
     */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDto") RegisterDto registerDto,
                           BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            authService.register(registerDto);
            return "redirect:/login?registered";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }
    }
}
