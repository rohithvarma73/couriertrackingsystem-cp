package com.wip.controller;

import com.wip.dto.TrackingUpdateDto;
import com.wip.exception.ResourceNotFoundException;
import com.wip.service.ShipmentService;
import com.wip.service.TrackingUpdateService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Thymeleaf UI controller for managing shipment tracking updates.
 *
 * <p>Handles browser-facing HTTP requests under the {@code /tracking} path and renders
 * server-side HTML views via Thymeleaf. This controller provides functionality to view
 * the tracking history of a shipment, add new tracking milestones (e.g., In Transit,
 * Delivered), and delete tracking updates.</p>
 *
 * @author Dharshan K S
 * @version 1.0
 * @since 1.0
 */
@Controller
@RequestMapping("/tracking")
public class TrackingUpdateUiController {

    private final TrackingUpdateService trackingUpdateService;
    private final ShipmentService shipmentService;

    /**
     * Constructs a {@code TrackingUpdateUiController} with the required services.
     *
     * @param trackingUpdateService the service responsible for tracking update logic
     * @param shipmentService       the service responsible for shipment data retrieval
     */
    public TrackingUpdateUiController(TrackingUpdateService trackingUpdateService,
                                      ShipmentService shipmentService) {
        this.trackingUpdateService = trackingUpdateService;
        this.shipmentService = shipmentService;
    }

    /**
     * Displays a list of all shipments to choose from for tracking.
     *
     * @param model the Spring MVC {@link Model} used to pass data to the view
     * @return the logical view name {@code "tracking/list"}
     */
    @GetMapping
    public String trackingHome(Model model) {
        model.addAttribute("shipments", shipmentService.getAllShipments());
        return "tracking/list";
    }

    /**
     * Displays the complete tracking history for a specific shipment.
     *
     * @param shipmentId the unique identifier of the shipment to track
     * @param model      the Spring MVC {@link Model} used to pass data to the view
     * @return the logical view name {@code "tracking/details"} on success, or {@code "error/not-found"} if the shipment does not exist
     */
    @GetMapping("/shipment/{shipmentId}")
    public String trackingByShipment(@PathVariable Long shipmentId, Model model) {
        try {
            model.addAttribute("shipment", shipmentService.getShipmentById(shipmentId));
            model.addAttribute("updates", trackingUpdateService.getTrackingUpdatesByShipmentId(shipmentId));
            return "tracking/details";
        } catch (ResourceNotFoundException ex) {
            model.addAttribute("errorTitle", "Shipment not found");
            model.addAttribute("errorMessage", ex.getMessage());
            return "error/not-found";
        }
    }

    /**
     * Displays a form for adding a new tracking update to a shipment.
     *
     * @param shipmentId the unique identifier of the shipment
     * @param model      the Spring MVC {@link Model} used to pass form-binding data to the view
     * @return the logical view name {@code "tracking/form"} on success, or {@code "error/not-found"} if the shipment does not exist
     */
    @GetMapping("/shipment/{shipmentId}/new")
    public String showAddUpdateForm(@PathVariable Long shipmentId, Model model) {
        try {
            model.addAttribute("shipment", shipmentService.getShipmentById(shipmentId));
            model.addAttribute("trackingUpdateDto", new TrackingUpdateDto());
            model.addAttribute("today", LocalDate.now());
            return "tracking/form";
        } catch (ResourceNotFoundException ex) {
            model.addAttribute("errorTitle", "Shipment not found");
            model.addAttribute("errorMessage", ex.getMessage());
            return "error/not-found";
        }
    }

    /**
     * Processes the form submission for a new tracking update.
     *
     * @param shipmentId        the unique identifier of the shipment
     * @param trackingUpdateDto the form-bound tracking update data
     * @param bindingResult     holds validation errors produced during binding
     * @param model             the Spring MVC {@link Model} used to pass error messages back to the view
     * @return a redirect to the tracking details page on success, or re-renders the form on error
     */
    @PostMapping("/shipment/{shipmentId}/save")
    public String saveTrackingUpdate(@PathVariable Long shipmentId,
                                     @Valid @ModelAttribute("trackingUpdateDto") TrackingUpdateDto trackingUpdateDto,
                                     BindingResult bindingResult,
                                     Model model) {
        if (bindingResult.hasErrors()) {
            try {
                model.addAttribute("shipment", shipmentService.getShipmentById(shipmentId));
            } catch (ResourceNotFoundException ignored) { /* handled below */ }
            model.addAttribute("today", LocalDate.now());
            return "tracking/form";
        }

        try {
            trackingUpdateService.addTrackingUpdate(shipmentId, trackingUpdateDto);
            return "redirect:/tracking/shipment/" + shipmentId;
        } catch (ResourceNotFoundException ex) {
            model.addAttribute("errorTitle", "Shipment not found");
            model.addAttribute("errorMessage", ex.getMessage());
            return "error/not-found";
        } catch (IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            try {
                model.addAttribute("shipment", shipmentService.getShipmentById(shipmentId));
            } catch (Exception ignored) { /* handled */ }
            model.addAttribute("today", LocalDate.now());
            return "tracking/form";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Could not save tracking update. Please try again.");
            try {
                model.addAttribute("shipment", shipmentService.getShipmentById(shipmentId));
            } catch (Exception ignored) { /* handled */ }
            model.addAttribute("today", LocalDate.now());
            return "tracking/form";
        }
    }

    /**
     * Deletes a specific tracking update.
     *
     * @param updateId           the unique identifier of the tracking update to delete
     * @param redirectAttributes Spring MVC redirect attributes to pass flash messages
     * @return a redirect to the associated shipment's tracking details on success, or tracking home on error
     */
    @PostMapping("/update/{updateId}/delete")
    public String deleteTrackingUpdate(@PathVariable Long updateId,
                                       RedirectAttributes redirectAttributes) {
        try {
            TrackingUpdateDto dto = trackingUpdateService.getTrackingUpdateById(updateId);
            trackingUpdateService.deleteTrackingUpdate(updateId);
            return "redirect:/tracking/shipment/" + dto.getShipmentId();
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tracking update not found.");
            return "redirect:/tracking";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not delete tracking update. Please try again.");
            return "redirect:/tracking";
        }
    }
}
