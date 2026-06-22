package com.wip.controller;

import com.wip.dto.CustomerDto;
import com.wip.security.CurrentUserUtil;
import com.wip.security.CustomUserDetails;
import com.wip.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Spring MVC controller that manages the user profile view and edit flows.
 *
 * <p>This controller handles all routes under {@code /profile}, allowing authenticated
 * users to view and update their customer profile information. It is role-aware:
 * administrator accounts do not have an associated customer record, so they are
 * directed to a dedicated admin-view page ({@code profile/admin-view}) instead of
 * the standard customer profile view. Regular users can view their profile at
 * {@code /profile} and edit it via the form at {@code /profile/edit}, which is
 * submitted to {@code /profile/update}.</p>
 *
 * <p>All mutations are guarded by Bean Validation on the {@link CustomerDto}, and
 * service-layer exceptions are caught and surfaced back to the edit form via the
 * {@code errorMessage} model attribute.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Controller
public class ProfileController {

    private final CustomerService customerService;

    /**
     * Constructs a {@code ProfileController} with the required customer service.
     *
     * @param customerService service used to retrieve and update customer profile data
     */
    public ProfileController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Retrieves the customer ID of the currently authenticated user from the security context.
     *
     * <p>The customer ID is stored inside the {@link CustomUserDetails} principal that
     * Spring Security places into the {@link Authentication} object at login time.
     * Returns {@code null} when the authentication principal is not an instance of
     * {@link CustomUserDetails} — this is the case for administrator accounts which
     * are not linked to a customer record.</p>
     *
     * @return the customer ID of the authenticated user, or {@code null} if the
     *         authenticated principal is not a {@link CustomUserDetails} (e.g., an admin)
     */
    private Long getLoggedInCustomerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getCustomerId();
        }
        return null;
    }

    /**
     * Handles GET requests to {@code /profile} and renders the profile view page.
     *
     * <p>The behaviour is role-dependent:</p>
     * <ul>
     *   <li><strong>Admins</strong> are directed to {@code profile/admin-view} with the
     *       model attribute {@code adminUsername} set to the current username.</li>
     *   <li><strong>Regular users</strong> with a valid customer ID receive the
     *       {@code customer} model attribute populated from the database and are
     *       directed to {@code profile/view}.</li>
     *   <li>If no customer ID can be resolved (unexpected state), the model attributes
     *       {@code errorTitle} and {@code errorMessage} are set and the view
     *       {@code error/not-found} is returned.</li>
     * </ul>
     *
     * @param model the Spring MVC {@link Model} used to pass profile data or error details
     *              to the view
     * @return the logical view name: {@code "profile/admin-view"} for admins,
     *         {@code "profile/view"} for regular users, or {@code "error/not-found"}
     *         if the customer record cannot be resolved
     */
    @GetMapping("/profile")
    public String viewProfile(Model model) {
        // Admins have no customer profile — show an info page instead of throwing an error
        if (CurrentUserUtil.isAdmin()) {
            model.addAttribute("adminUsername", CurrentUserUtil.getCurrentUsername());
            return "profile/admin-view";
        }

        Long customerId = getLoggedInCustomerId();
        if (customerId == null) {
            // Authenticated user with no customer record (should not normally happen)
            model.addAttribute("errorTitle", "Profile not found");
            model.addAttribute("errorMessage",
                    "Your account does not have a customer profile linked. Please contact support.");
            return "error/not-found";
        }
        model.addAttribute("customer", customerService.getCustomerById(customerId));
        return "profile/view";
    }

    /**
     * Handles GET requests to {@code /profile/edit} and renders the profile edit form.
     *
     * <p>Admin users are redirected to {@code /profile} since they do not possess a
     * customer record to edit. Regular users have their existing profile data pre-populated
     * into the form via the {@code customerDto} model attribute so that current values
     * are visible in the form fields.</p>
     *
     * @param model the Spring MVC {@link Model} used to pre-populate the edit form
     *              with the existing {@link CustomerDto} data
     * @return {@code "redirect:/profile"} for admins, {@code "redirect:/login"} if no
     *         customer ID can be resolved, or the logical view name {@code "profile/edit"}
     *         for regular users
     */
    @GetMapping("/profile/edit")
    public String editProfileForm(Model model) {
        if (CurrentUserUtil.isAdmin()) {
            return "redirect:/profile";
        }

        Long customerId = getLoggedInCustomerId();
        if (customerId == null) {
            return "redirect:/login";
        }
        model.addAttribute("customerDto", customerService.getCustomerById(customerId));
        return "profile/edit";
    }

    /**
     * Handles POST requests to {@code /profile/update} and saves the edited profile data.
     *
     * <p>The submitted {@link CustomerDto} is validated using Bean Validation constraints.
     * If validation fails, the edit form is re-rendered with the binding errors. On
     * success, the update is delegated to {@link CustomerService#updateCustomer(Long, CustomerDto)}
     * and the user is redirected to {@code /profile?updated} so the view can display a
     * success notification. Service-layer exceptions are caught and surfaced via the
     * {@code errorMessage} model attribute without leaving the edit form.</p>
     *
     * @param customerDto   the form-backed DTO containing the updated profile fields;
     *                      validated via {@code @Valid}
     * @param bindingResult holds any Bean Validation errors detected for {@code customerDto}
     * @param model         the Spring MVC {@link Model} used to pass error messages back
     *                      to the edit form on failure
     * @return {@code "redirect:/profile"} for admins, {@code "redirect:/login"} if no
     *         customer ID can be resolved, {@code "redirect:/profile?updated"} on
     *         successful save, or {@code "profile/edit"} if validation or a service
     *         exception occurs
     */
    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("customerDto") CustomerDto customerDto,
                                BindingResult bindingResult,
                                Model model) {
        if (CurrentUserUtil.isAdmin()) {
            return "redirect:/profile";
        }

        if (bindingResult.hasErrors()) {
            return "profile/edit";
        }
        Long customerId = getLoggedInCustomerId();
        if (customerId == null) {
            return "redirect:/login";
        }
        try {
            customerService.updateCustomer(customerId, customerDto);
            return "redirect:/profile?updated";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Could not save your profile. Please try again.");
            return "profile/edit";
        }
    }
}

