package com.wip.controller;

import com.wip.dto.ParcelDto;
import com.wip.exception.ResourceNotFoundException;
import com.wip.service.ParcelService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Thymeleaf UI controller for managing parcel bookings in the Courier Tracking System.
 *
 * <p>Handles browser-facing HTTP requests under the {@code /parcels} path and renders
 * server-side HTML views via Thymeleaf. This controller covers the full parcel lifecycle —
 * listing all parcels, filtering by customer, creating new bookings, viewing details,
 * editing, and deleting parcel records.</p>
 *
 * <p>Security-aware: for non-admin users, the customer ID is automatically resolved from
 * the authenticated Spring Security principal ({@code CustomUserDetails}) so that regular
 * customers can only create and update parcels linked to their own account.</p>
 *
 * <p>Page flow overview:
 * <ul>
 *   <li>{@code GET  /parcels}                        → {@code parcel/list}        — all parcels</li>
 *   <li>{@code GET  /parcels/by-customer/{customerId}} → {@code parcel/by-customer} — parcels for one customer</li>
 *   <li>{@code GET  /parcels/new}                    → {@code parcel/form}        — blank creation form</li>
 *   <li>{@code POST /parcels/save}                   → redirect to details or re-render form on error</li>
 *   <li>{@code GET  /parcels/{id}}                   → {@code parcel/details}     — single parcel view</li>
 *   <li>{@code GET  /parcels/{id}/edit}              → {@code parcel/form}        — pre-filled edit form</li>
 *   <li>{@code POST /parcels/{id}/update}            → redirect to details or re-render form on error</li>
 *   <li>{@code POST /parcels/{id}/delete}            → redirect to list</li>
 * </ul>
 *
 * @author Dharshan K S (dharshan.ks@wipro.com)
 * @version 1.0
 * @since 1.0
 */
@Controller
@RequestMapping("/parcels")
public class ParcelUiController {

    /** Service layer delegate for all parcel business operations. */
    private final ParcelService parcelService;

    /**
     * Constructs a {@code ParcelUiController} with the required {@link ParcelService}.
     *
     * @param parcelService the service bean responsible for parcel business logic;
     *                      injected by Spring's dependency injection container
     */
    public ParcelUiController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    /**
     * Displays the list of all parcel bookings in the system.
     *
     * <p>Fetches every parcel record and adds them to the model as a {@code parcels}
     * attribute before rendering the list view.</p>
     *
     * @param model the Spring MVC {@link Model} used to pass the {@code parcels} attribute
     *              (a list of {@link ParcelDto}) to the view
     * @return the logical view name {@code "parcel/list"}
     */
    @GetMapping
    public String listParcels(Model model) {
        model.addAttribute("parcels", parcelService.getAllParcels());
        return "parcel/list";
    }

    /**
     * Displays the list of parcel bookings belonging to a specific customer.
     *
     * <p>Fetches parcels filtered by the given {@code customerId} and renders them
     * in the by-customer view. Both the parcel list and the customer ID are added
     * to the model for display and navigation purposes.</p>
     *
     * @param customerId the unique identifier of the customer whose parcels should be listed
     * @param model      the Spring MVC {@link Model}; receives {@code parcels} (list of
     *                   {@link ParcelDto}) and {@code customerId} attributes
     * @return the logical view name {@code "parcel/by-customer"}
     */
    @GetMapping("/by-customer/{customerId}")
    public String listParcelsByCustomer(@PathVariable Long customerId, Model model) {
        model.addAttribute("parcels", parcelService.getParcelsByCustomerId(customerId));
        model.addAttribute("customerId", customerId);
        return "parcel/by-customer";
    }

    /**
     * Displays a blank form for creating a new parcel booking.
     *
     * <p>For regular (non-admin) users, the customer ID is automatically populated from
     * the authenticated Spring Security principal so that the user cannot create parcels
     * on behalf of another customer. Admin users may optionally pre-populate the customer ID
     * via the {@code customerId} request parameter. The current date is also added to the
     * model to support date-picker defaults in the form.</p>
     *
     * @param customerId an optional customer ID to pre-fill on the form; used by admins
     *                   when navigating from a customer's detail page (may be {@code null})
     * @param model      the Spring MVC {@link Model}; receives {@code parcelDto} (an empty
     *                   {@link ParcelDto}) and {@code today} ({@link LocalDate}) attributes
     * @return the logical view name {@code "parcel/form"}
     */
    @GetMapping("/new")
    public String showCreateForm(@RequestParam(value = "customerId", required = false) Long customerId,
                                 Model model) {
        ParcelDto parcelDto = new ParcelDto();

        // Auto-set customer from auth if regular user
        if (!com.wip.security.CurrentUserUtil.isAdmin()) {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder
                            .getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.wip.security.CustomUserDetails details) {
                parcelDto.setCustomerId(details.getCustomerId());
            }
        } else if (customerId != null) {
            parcelDto.setCustomerId(customerId);
        }

        model.addAttribute("parcelDto", parcelDto);
        model.addAttribute("today", LocalDate.now());
        return "parcel/form";
    }

    /**
     * Processes the submission of the new parcel creation form and persists the booking.
     *
     * <p>For non-admin users, the customer ID is re-enforced from the authenticated
     * Spring Security principal to prevent tampering with hidden form fields. Bean Validation
     * is then applied; on failure the form is re-rendered with inline error messages. On
     * successful persistence, the user is redirected to the new parcel's detail page.</p>
     *
     * <p>Handles {@link ResourceNotFoundException} (e.g., customer not found or user has
     * no linked customer profile) and generic exceptions, adding descriptive error messages
     * to the model in both cases.</p>
     *
     * @param parcelDto     the form-bound parcel data submitted by the user;
     *                      must satisfy all Bean Validation constraints
     * @param bindingResult holds validation errors produced during binding and validation
     * @param model         the Spring MVC {@link Model} used to pass error messages and the
     *                      {@code today} date attribute back to the view
     * @return a redirect to {@code /parcels/{id}} on success, or the logical view name
     *         {@code "parcel/form"} when validation or persistence fails
     */
    @PostMapping("/save")
    public String saveParcel(@Valid @ModelAttribute("parcelDto") ParcelDto parcelDto,
                             BindingResult bindingResult,
                             Model model) {
        // Re-enforce customer ID from auth for non-admin users
        if (!com.wip.security.CurrentUserUtil.isAdmin()) {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder
                            .getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.wip.security.CustomUserDetails details) {
                parcelDto.setCustomerId(details.getCustomerId());
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("today", LocalDate.now());
            return "parcel/form";
        }

        try {
            ParcelDto saved = parcelService.addParcel(parcelDto);
            return "redirect:/parcels/" + saved.getParcelId();
        } catch (ResourceNotFoundException ex) {
            // e.g. customer not found or user has no profile yet
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("today", LocalDate.now());
            return "parcel/form";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Could not save the parcel. Please check your details and try again.");
            model.addAttribute("today", LocalDate.now());
            return "parcel/form";
        }
    }

    /**
     * Displays the detail view for a single parcel booking identified by its ID.
     *
     * <p>Fetches the parcel by {@code id} and adds it as a {@code parcel} model attribute.
     * If the parcel does not exist, the service layer propagates a
     * {@code ResourceNotFoundException}.</p>
     *
     * @param id    the unique identifier of the parcel whose details are to be displayed
     * @param model the Spring MVC {@link Model}; receives the {@code parcel} attribute
     *              (a {@link ParcelDto}) for rendering
     * @return the logical view name {@code "parcel/details"}
     * @throws com.wip.exception.ResourceNotFoundException if no parcel exists with the given ID
     */
    @GetMapping("/{id}")
    public String parcelDetails(@PathVariable Long id, Model model) {
        model.addAttribute("parcel", parcelService.getParcelById(id));
        return "parcel/details";
    }

    /**
     * Displays a pre-populated form for editing an existing parcel booking.
     *
     * <p>Loads the current parcel data by {@code id} and binds it to the {@code parcelDto}
     * model attribute so that all existing field values appear pre-filled in the form. The
     * current date is also provided to support date-picker defaults.</p>
     *
     * @param id    the unique identifier of the parcel to be edited
     * @param model the Spring MVC {@link Model}; receives {@code parcelDto} (pre-filled
     *              {@link ParcelDto}) and {@code today} ({@link LocalDate}) attributes
     * @return the logical view name {@code "parcel/form"}
     * @throws com.wip.exception.ResourceNotFoundException if no parcel exists with the given ID
     */
    @GetMapping("/{id}/edit")
    public String editParcelForm(@PathVariable Long id, Model model) {
        model.addAttribute("parcelDto", parcelService.getParcelById(id));
        model.addAttribute("today", LocalDate.now());
        return "parcel/form";
    }

    /**
     * Processes the submission of the parcel edit form and persists the updated booking details.
     *
     * <p>For non-admin users, the customer ID is re-enforced from the authenticated
     * Spring Security principal to prevent hidden-field tampering. Bean Validation is
     * applied; on failure the form is re-rendered with inline error messages. On success,
     * the user is redirected to the parcel's detail page.</p>
     *
     * @param id            the unique identifier of the parcel being updated
     * @param parcelDto     the form-bound updated parcel data; must satisfy all Bean Validation constraints
     * @param bindingResult holds validation errors produced during binding and validation
     * @param model         the Spring MVC {@link Model} used to pass error messages and the
     *                      {@code today} date attribute back to the view
     * @return a redirect to {@code /parcels/{id}} on success, or the logical view name
     *         {@code "parcel/form"} when validation or persistence fails
     * @throws com.wip.exception.ResourceNotFoundException if no parcel exists with the given ID
     */
    @PostMapping("/{id}/update")
    public String updateParcel(@PathVariable Long id,
                               @Valid @ModelAttribute("parcelDto") ParcelDto parcelDto,
                               BindingResult bindingResult,
                               Model model) {
        // Re-enforce customer ID from auth for non-admin users (hidden field may be missing)
        if (!com.wip.security.CurrentUserUtil.isAdmin()) {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder
                            .getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.wip.security.CustomUserDetails details) {
                parcelDto.setCustomerId(details.getCustomerId());
            }
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("today", LocalDate.now());
            return "parcel/form";
        }

        try {
            parcelService.updateParcel(id, parcelDto);
            return "redirect:/parcels/" + id;
        } catch (ResourceNotFoundException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("today", LocalDate.now());
            return "parcel/form";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Could not update the parcel. Please try again.");
            model.addAttribute("today", LocalDate.now());
            return "parcel/form";
        }
    }

    /**
     * Handles the deletion of a parcel booking and redirects appropriately.
     *
     * <p>Attempts to delete the parcel identified by {@code id}. On success, a flash
     * attribute {@code successMessage} is added and the user is redirected to the parcel
     * list. If the parcel does not exist, the exception message is surfaced as a flash
     * error. Any other unexpected exception also results in a redirect to the parcel list
     * with a generic error flash message.</p>
     *
     * @param id                 the unique identifier of the parcel to delete
     * @param redirectAttributes Spring MVC redirect attribute container used to carry
     *                           flash messages across the redirect boundary
     * @return a redirect to {@code /parcels} in all cases
     */
    @PostMapping("/{id}/delete")
    public String deleteParcel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            parcelService.deleteParcel(id);
            redirectAttributes.addFlashAttribute("successMessage", "Parcel deleted successfully.");
            return "redirect:/parcels";
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/parcels";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not delete this parcel. Please try again.");
            return "redirect:/parcels";
        }
    }
}
