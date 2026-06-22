package com.wip.controller;

import com.wip.dto.ShipmentDto;
import com.wip.exception.ResourceNotFoundException;
import com.wip.service.ParcelService;
import com.wip.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Thymeleaf UI controller for managing shipments in the Courier Tracking System.
 *
 * <p>Handles browser-facing HTTP requests under the {@code /shipments} path and renders
 * server-side HTML views via Thymeleaf. This controller covers the complete shipment
 * lifecycle — listing all shipments, initiating new shipments from a parcel, displaying
 * shipment details, editing, and deleting shipment records.</p>
 *
 * <p>Duplicate-shipment handling: if a shipment already exists for a parcel (detected
 * via {@link IllegalStateException} thrown by the service), the controller gracefully
 * looks up the existing shipment and redirects the user to its detail page rather than
 * showing a raw error.</p>
 *
 * <p>Page flow overview:
 * <ul>
 *   <li>{@code GET  /shipments}                    → {@code shipment/list}    — all shipments</li>
 *   <li>{@code GET  /shipments/new}                → {@code shipment/form}    — blank creation form</li>
 *   <li>{@code POST /shipments/save}               → redirect to details or re-render form on error</li>
 *   <li>{@code GET  /shipments/by-parcel/{parcelId}} → {@code shipment/start} — start-shipment confirmation page</li>
 *   <li>{@code POST /shipments/start/{parcelId}}   → redirect to details or back to confirmation on error</li>
 *   <li>{@code GET  /shipments/{id}}               → {@code shipment/details} — single shipment view</li>
 *   <li>{@code GET  /shipments/{id}/edit}          → {@code shipment/form}    — pre-filled edit form</li>
 *   <li>{@code POST /shipments/{id}/update}        → redirect to details or re-render form on error</li>
 *   <li>{@code POST /shipments/{id}/delete}        → redirect to list or back to details on error</li>
 * </ul>
 *
 * @author Dharshan K S (dharshan.ks@wipro.com)
 * @version 1.0
 * @since 1.0
 */
@Controller
@RequestMapping("/shipments")
public class ShipmentUiController {

    /** Service layer delegate for all shipment business operations. */
    private final ShipmentService shipmentService;

    /** Service layer delegate used to look up parcel details for the start-shipment page. */
    private final ParcelService parcelService;

    /**
     * Constructs a {@code ShipmentUiController} with the required service dependencies.
     *
     * @param shipmentService the service bean responsible for shipment business logic;
     *                        injected by Spring's dependency injection container
     * @param parcelService   the service bean used to retrieve parcel data for the
     *                        shipment confirmation page
     */
    public ShipmentUiController(ShipmentService shipmentService, ParcelService parcelService) {
        this.shipmentService = shipmentService;
        this.parcelService = parcelService;
    }

    /**
     * Displays the list of all shipments in the system.
     *
     * <p>Fetches every shipment record and adds it to the model as a {@code shipments}
     * attribute before rendering the list view.</p>
     *
     * @param model the Spring MVC {@link Model} used to pass the {@code shipments} attribute
     *              (a list of {@link ShipmentDto}) to the view
     * @return the logical view name {@code "shipment/list"}
     */
    @GetMapping
    public String listShipments(Model model) {
        model.addAttribute("shipments", shipmentService.getAllShipments());
        return "shipment/list";
    }

    /**
     * Displays a blank form for creating a new shipment.
     *
     * <p>Adds an empty {@link ShipmentDto} and the current date to the model to support
     * form binding and date-picker defaults in the Thymeleaf template.</p>
     *
     * @param model the Spring MVC {@link Model}; receives {@code shipmentDto} (an empty
     *              {@link ShipmentDto}) and {@code today} ({@link LocalDate}) attributes
     * @return the logical view name {@code "shipment/form"}
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("shipmentDto", new ShipmentDto());
        model.addAttribute("today", LocalDate.now());
        return "shipment/form";
    }

    /**
     * Processes the submission of the new shipment creation form and initiates the shipment.
     *
     * <p>Delegates to the service layer using the parcel ID bound in the submitted
     * {@link ShipmentDto}. If an {@link IllegalStateException} is thrown (indicating a
     * shipment already exists for this parcel), the controller attempts to locate the
     * existing shipment and redirect to its detail page. If the parcel is not found, an
     * appropriate error message is set and the form is re-rendered.</p>
     *
     * @param shipmentDto        the form-bound shipment data; only {@code parcelId} is used
     *                           by the service to initiate the shipment
     * @param model              the Spring MVC {@link Model} used to pass error messages and
     *                           the {@code today} date attribute back to the view
     * @param redirectAttributes Spring MVC redirect attribute container used to carry
     *                           flash messages across the redirect boundary
     * @return a redirect to {@code /shipments/{id}} on success; or the logical view name
     *         {@code "shipment/form"} when an error occurs
     */
    @PostMapping("/save")
    public String saveShipment(@ModelAttribute("shipmentDto") ShipmentDto shipmentDto,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            ShipmentDto saved = shipmentService.addShipment(shipmentDto.getParcelId());
            return "redirect:/shipments/" + saved.getShipmentId();
        } catch (IllegalStateException ex) {
            // "Shipment already exists" or "only admins can add shipments"
            // Try to find the existing shipment and redirect to it
            try {
                ShipmentDto existing = shipmentService.getShipmentByParcelId(shipmentDto.getParcelId());
                if (existing != null) {
                    redirectAttributes.addFlashAttribute("infoMessage",
                            "A shipment already exists for this parcel.");
                    return "redirect:/shipments/" + existing.getShipmentId();
                }
            } catch (Exception ignored) { /* fall through */ }

            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("shipmentDto", shipmentDto);
            model.addAttribute("today", LocalDate.now());
            return "shipment/form";
        } catch (ResourceNotFoundException ex) {
            model.addAttribute("errorMessage", "Parcel not found: " + ex.getMessage());
            model.addAttribute("shipmentDto", shipmentDto);
            model.addAttribute("today", LocalDate.now());
            return "shipment/form";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Could not create the shipment. Please try again.");
            model.addAttribute("shipmentDto", shipmentDto);
            model.addAttribute("today", LocalDate.now());
            return "shipment/form";
        }
    }

    /**
     * Shows a confirmation page before starting a shipment from the parcel details view.
     *
     * <p>Loads the parcel identified by {@code parcelId} and checks whether a shipment
     * already exists for it. If an existing shipment is found, {@code shipmentAlreadyExists}
     * is set to {@code true} and the existing shipment details are included in the model so
     * that the view can render an appropriate message or redirect link. If the parcel is not
     * found, the {@code error/not-found} view is rendered.</p>
     *
     * @param parcelId the unique identifier of the parcel for which shipment creation is being considered
     * @param model    the Spring MVC {@link Model}; receives {@code parcel}, {@code parcelId},
     *                 {@code shipmentAlreadyExists}, and optionally {@code existingShipment} attributes
     * @return the logical view name {@code "shipment/start"} on success; {@code "error/not-found"}
     *         if the parcel does not exist; or {@code "error/general"} for unexpected errors
     */
    @GetMapping("/by-parcel/{parcelId}")
    public String showStartShipmentPage(@PathVariable Long parcelId, Model model) {
        try {
            var parcel = parcelService.getParcelById(parcelId);
            model.addAttribute("parcel", parcel);
            model.addAttribute("parcelId", parcelId);

            ShipmentDto existing = shipmentService.getShipmentByParcelId(parcelId);
            if (existing != null) {
                model.addAttribute("shipmentAlreadyExists", true);
                model.addAttribute("existingShipment", existing);
            } else {
                model.addAttribute("shipmentAlreadyExists", false);
            }

            return "shipment/start";
        } catch (ResourceNotFoundException ex) {
            model.addAttribute("errorTitle", "Parcel not found");
            model.addAttribute("errorMessage", ex.getMessage());
            return "error/not-found";
        } catch (Exception ex) {
            model.addAttribute("errorTitle", "Something went wrong");
            model.addAttribute("errorMessage", "Could not load parcel details. Please try again.");
            return "error/general";
        }
    }

    /**
     * Handles the POST request when an admin confirms shipment creation from the confirmation page.
     *
     * <p>Creates a new shipment for the parcel identified by {@code parcelId}. If a shipment
     * already exists (service throws {@link IllegalStateException}), the controller attempts
     * to locate the existing shipment and redirects the user to its detail page with an
     * informational flash message. Any other exception redirects back to the confirmation page
     * with an error flash message.</p>
     *
     * @param parcelId           the unique identifier of the parcel for which the shipment is to be started
     * @param redirectAttributes Spring MVC redirect attribute container used to carry
     *                           flash messages across the redirect boundary
     * @return a redirect to {@code /shipments/{id}} on success; a redirect to the existing
     *         shipment's detail page if one already exists; or a redirect back to
     *         {@code /shipments/by-parcel/{parcelId}} on other errors
     */
    @PostMapping("/start/{parcelId}")
    public String createShipmentFromParcel(@PathVariable Long parcelId,
                                           RedirectAttributes redirectAttributes) {
        try {
            ShipmentDto saved = shipmentService.addShipment(parcelId);
            return "redirect:/shipments/" + saved.getShipmentId();
        } catch (IllegalStateException ex) {
            // Shipment already exists — find it and redirect
            try {
                ShipmentDto existing = shipmentService.getShipmentByParcelId(parcelId);
                if (existing != null) {
                    redirectAttributes.addFlashAttribute("infoMessage",
                            "A shipment already exists for this parcel. Showing the existing one.");
                    return "redirect:/shipments/" + existing.getShipmentId();
                }
            } catch (Exception ignored) { /* fall through */ }

            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/shipments/by-parcel/" + parcelId;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not start the shipment. Please try again.");
            return "redirect:/shipments/by-parcel/" + parcelId;
        }
    }

    /**
     * Displays the detail view for a single shipment identified by its ID.
     *
     * <p>Fetches the shipment by {@code id} and adds it as a {@code shipment} model
     * attribute for rendering. If the shipment does not exist, the service layer
     * propagates a {@code ResourceNotFoundException}.</p>
     *
     * @param id    the unique identifier of the shipment to display
     * @param model the Spring MVC {@link Model}; receives the {@code shipment} attribute
     *              (a {@link ShipmentDto}) for rendering
     * @return the logical view name {@code "shipment/details"}
     * @throws com.wip.exception.ResourceNotFoundException if no shipment exists with the given ID
     */
    @GetMapping("/{id}")
    public String shipmentDetails(@PathVariable Long id, Model model) {
        model.addAttribute("shipment", shipmentService.getShipmentById(id));
        return "shipment/details";
    }

    /**
     * Displays a pre-populated form for editing an existing shipment.
     *
     * <p>Loads the current shipment data by {@code id} and binds it to the
     * {@code shipmentDto} model attribute so that existing field values appear
     * pre-filled in the Thymeleaf form. The current date is also provided.</p>
     *
     * @param id    the unique identifier of the shipment to be edited
     * @param model the Spring MVC {@link Model}; receives {@code shipmentDto} (pre-filled
     *              {@link ShipmentDto}) and {@code today} ({@link LocalDate}) attributes
     * @return the logical view name {@code "shipment/form"}
     * @throws com.wip.exception.ResourceNotFoundException if no shipment exists with the given ID
     */
    @GetMapping("/{id}/edit")
    public String editShipmentForm(@PathVariable Long id, Model model) {
        model.addAttribute("shipmentDto", shipmentService.getShipmentById(id));
        model.addAttribute("today", LocalDate.now());
        return "shipment/form";
    }

    /**
     * Processes the submission of the shipment edit form and persists the updated details.
     *
     * <p>Validates the submitted {@link ShipmentDto} via Bean Validation. On validation
     * failure, the form is re-rendered with inline error messages. On success, the user is
     * redirected to the shipment's detail page. Business-rule violations (e.g. invalid
     * status transition) are caught as {@link IllegalStateException} and surfaced with a
     * descriptive error message.</p>
     *
     * @param id            the unique identifier of the shipment being updated
     * @param shipmentDto   the form-bound updated shipment data; must satisfy Bean Validation constraints
     * @param bindingResult holds validation errors produced during binding and validation
     * @param model         the Spring MVC {@link Model} used to pass error messages and the
     *                      {@code today} date attribute back to the view
     * @return a redirect to {@code /shipments/{id}} on success, or the logical view name
     *         {@code "shipment/form"} when validation or persistence fails
     */
    @PostMapping("/{id}/update")
    public String updateShipment(@PathVariable Long id,
                                 @Valid @ModelAttribute("shipmentDto") ShipmentDto shipmentDto,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("today", LocalDate.now());
            return "shipment/form";
        }
        try {
            shipmentService.updateShipment(id, shipmentDto);
            return "redirect:/shipments/" + id;
        } catch (IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("today", LocalDate.now());
            return "shipment/form";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Could not update the shipment. Please try again.");
            model.addAttribute("today", LocalDate.now());
            return "shipment/form";
        }
    }

    /**
     * Handles the deletion of a shipment and redirects appropriately.
     *
     * <p>Attempts to delete the shipment identified by {@code id}. On success, a flash
     * attribute {@code successMessage} is added and the user is redirected to the shipment
     * list. If a business rule prevents deletion (service throws {@link IllegalStateException},
     * e.g. shipment is in an active state), the user is redirected back to the shipment's
     * detail page with an error flash message. Any other exception also redirects to the
     * shipment list with a generic error message.</p>
     *
     * @param id                 the unique identifier of the shipment to delete
     * @param redirectAttributes Spring MVC redirect attribute container used to carry
     *                           flash messages across the redirect boundary
     * @return a redirect to {@code /shipments} on success or generic error; or a redirect
     *         to {@code /shipments/{id}} when a business-rule violation prevents deletion
     */
    @PostMapping("/{id}/delete")
    public String deleteShipment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            shipmentService.deleteShipment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Shipment deleted.");
            return "redirect:/shipments";
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/shipments/" + id;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not delete this shipment. Please try again.");
            return "redirect:/shipments";
        }
    }
}
