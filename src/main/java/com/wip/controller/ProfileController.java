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
 * ProfileController Component.
 * 
 * Handles operations and data related to ProfileController.
 */
@Controller
public class ProfileController {

    private final CustomerService customerService;

    public ProfileController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Returns the customerId of the currently authenticated user,
     * or null if the user is not authenticated or has no linked customer (e.g. admin).
     */
    private Long getLoggedInCustomerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getCustomerId();
        }
        return null;
    }

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        // Admins have no customer profile â€” show an info page instead of throwing an error
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

