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

@Controller
@RequestMapping("/tracking")
public class TrackingUpdateUiController {

    private final TrackingUpdateService trackingUpdateService;
    private final ShipmentService shipmentService;

    public TrackingUpdateUiController(TrackingUpdateService trackingUpdateService,
                                      ShipmentService shipmentService) {
        this.trackingUpdateService = trackingUpdateService;
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public String trackingHome(Model model) {
        model.addAttribute("shipments", shipmentService.getAllShipments());
        return "tracking/list";
    }

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