package com.wip.controller;

import com.wip.dto.ShipmentDto;
import com.wip.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/shipments")
public class ShipmentUiController {

    private final ShipmentService shipmentService;

    public ShipmentUiController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public String listShipments(Model model) {
        model.addAttribute("shipments", shipmentService.getAllShipments());
        return "shipment/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("shipmentDto", new ShipmentDto());
        return "shipment/form";
    }

    @PostMapping("/save")
    public String saveShipment(@ModelAttribute("shipmentDto") @Valid ShipmentDto shipmentDto) {
        ShipmentDto saved = shipmentService.addShipment(shipmentDto.getParcelId());
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
        return "shipment/form";
    }

    @PostMapping("/{id}/update")
    public String updateShipment(@PathVariable Long id,
                                 @ModelAttribute("shipmentDto") @Valid ShipmentDto shipmentDto) {
        shipmentService.updateShipment(id, shipmentDto);
        return "redirect:/shipments/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return "redirect:/shipments";
    }
}