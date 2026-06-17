package com.wip.controller;

import com.wip.dto.ShipmentDto;
import com.wip.service.ParcelService;
import com.wip.service.ShipmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
        ShipmentDto shipmentDto = new ShipmentDto();
        model.addAttribute("shipmentDto", shipmentDto);
        model.addAttribute("today", LocalDate.now());
        return "shipment/form";
    }

    @PostMapping("/save")
    public String saveShipment(@ModelAttribute("shipmentDto") ShipmentDto shipmentDto) {
        ShipmentDto saved = shipmentService.addShipment(shipmentDto.getParcelId());
        return "redirect:/shipments/" + saved.getShipmentId();
    }

    @GetMapping("/by-parcel/{parcelId}")
    public String showStartShipmentPage(@PathVariable Long parcelId, Model model) {
        try {
            var parcel = parcelService.getParcelById(parcelId);
            var shipment = shipmentService.getShipmentByParcelId(parcelId);

            model.addAttribute("parcel", parcel);
            model.addAttribute("parcelId", parcelId);

            if (shipment != null) {
                model.addAttribute("shipmentAlreadyExists", true);
                model.addAttribute("existingShipment", shipment);
            } else {
                model.addAttribute("shipmentAlreadyExists", false);
            }

            return "shipment/start";
        } catch (Exception ex) {
            model.addAttribute("errorTitle", "Parcel not found");
            model.addAttribute("errorMessage", "The parcel you are trying to start shipment for does not exist.");
            return "error/parcel-not-found";
        }
    }

    @PostMapping("/start/{parcelId}")
    public String createShipmentFromParcel(@PathVariable Long parcelId) {
        ShipmentDto saved = shipmentService.addShipment(parcelId);
        return "redirect:/shipments/" + saved.getShipmentId();
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
                                 @ModelAttribute("shipmentDto") ShipmentDto shipmentDto) {
        shipmentService.updateShipment(id, shipmentDto);
        return "redirect:/shipments/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return "redirect:/shipments";
    }
}