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
 * ShipmentUiController Component.
 * 
 * Handles operations and data related to ShipmentUiController.
 */
@Controller
@RequestMapping("/shipments")
public class ShipmentUiController {

    private final ShipmentService shipmentService;
    private final ParcelService parcelService;

    public ShipmentUiController(ShipmentService shipmentService, ParcelService parcelService) {
        this.shipmentService = shipmentService;
        this.parcelService = parcelService;
    }

    @GetMapping
    public String listShipments(Model model) {
        model.addAttribute("shipments", shipmentService.getAllShipments());
        return "shipment/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("shipmentDto", new ShipmentDto());
        model.addAttribute("today", LocalDate.now());
        return "shipment/form";
    }

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
     * Handles the case where a shipment already exists gracefully.
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
     * POST handler when admin clicks "Start shipment" on the confirmation page.
     */
    @PostMapping("/start/{parcelId}")
    public String createShipmentFromParcel(@PathVariable Long parcelId,
                                           RedirectAttributes redirectAttributes) {
        try {
            ShipmentDto saved = shipmentService.addShipment(parcelId);
            return "redirect:/shipments/" + saved.getShipmentId();
        } catch (IllegalStateException ex) {
            // Shipment already exists â€” find it and redirect
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

    @GetMapping("/{id}")
    public String shipmentDetails(@PathVariable Long id, Model model) {
        model.addAttribute("shipment", shipmentService.getShipmentById(id));
        return "shipment/details";
    }

    @GetMapping("/{id}/edit")
    public String editShipmentForm(@PathVariable Long id, Model model) {
        model.addAttribute("shipmentDto", shipmentService.getShipmentById(id));
        model.addAttribute("today", LocalDate.now());
        return "shipment/form";
    }

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
