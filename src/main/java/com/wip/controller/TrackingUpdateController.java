package com.wip.controller;

import com.wip.dto.TrackingUpdateDto;
import com.wip.service.TrackingUpdateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TrackingUpdateController Component.
 * 
 * Handles operations and data related to TrackingUpdateController.
 */
@RestController
@RequestMapping("/api/shipments")
public class TrackingUpdateController {

    private final TrackingUpdateService trackingUpdateService;

    public TrackingUpdateController(TrackingUpdateService trackingUpdateService) {
        this.trackingUpdateService = trackingUpdateService;
    }

    @PostMapping("/{shipmentId}/tracking-updates")
    public ResponseEntity<TrackingUpdateDto> addTrackingUpdate(
            @PathVariable Long shipmentId,
            @Valid @RequestBody TrackingUpdateDto trackingUpdateDto) {
        return ResponseEntity.ok(trackingUpdateService.addTrackingUpdate(shipmentId, trackingUpdateDto));
    }

    @GetMapping("/{shipmentId}/tracking-updates")
    public ResponseEntity<List<TrackingUpdateDto>> getTrackingUpdatesByShipmentId(
            @PathVariable Long shipmentId) {
        return ResponseEntity.ok(trackingUpdateService.getTrackingUpdatesByShipmentId(shipmentId));
    }
}
