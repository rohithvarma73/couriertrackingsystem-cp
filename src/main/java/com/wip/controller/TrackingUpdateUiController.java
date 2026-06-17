package com.wip.controller;

import com.wip.dto.TrackingUpdateDto;
import com.wip.service.ShipmentService;
import com.wip.service.TrackingUpdateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("shipment", shipmentService.getShipmentById(shipmentId));
        model.addAttribute("updates", trackingUpdateService.getTrackingUpdatesByShipmentId(shipmentId));
        return "tracking/details";
    }

    @GetMapping("/shipment/{shipmentId}/new")
    public String showAddUpdateForm(@PathVariable Long shipmentId, Model model) {
        model.addAttribute("shipment", shipmentService.getShipmentById(shipmentId));
        model.addAttribute("trackingUpdateDto", new TrackingUpdateDto());
        return "tracking/form";
    }

    @PostMapping("/shipment/{shipmentId}/save")
    public String saveTrackingUpdate(@PathVariable Long shipmentId,
                                     @ModelAttribute("trackingUpdateDto") TrackingUpdateDto trackingUpdateDto) {
        trackingUpdateService.addTrackingUpdate(shipmentId, trackingUpdateDto);
        return "redirect:/tracking/shipment/" + shipmentId;
    }

    @DeleteMapping("/update/{updateId}/delete")
    public String deleteTrackingUpdate(@PathVariable Long updateId) {
        TrackingUpdateDto dto = trackingUpdateService.getTrackingUpdateById(updateId);
        trackingUpdateService.deleteTrackingUpdate(updateId);
        return "redirect:/tracking/shipment/" + dto.getShipmentId();
    }
}