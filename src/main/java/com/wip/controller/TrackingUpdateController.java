package com.wip.controller;

import com.wip.dto.TrackingUpdateDto;
import com.wip.service.TrackingUpdateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing tracking updates associated with shipments.
 *
 * <p>Exposes RESTful endpoints nested under {@code /api/shipments/{shipmentId}/tracking-updates}
 * that allow authorised clients to record and retrieve real-time status updates for a
 * given shipment. Each tracking update captures a location, timestamp, and status
 * milestone (e.g., "In Transit", "Out for Delivery", "Delivered") for the shipment's
 * journey. This controller shares the {@code /api/shipments} base path with
 * {@link ShipmentController} and focuses exclusively on the tracking-update sub-resource.</p>
 *
 * @author Dharshan K S (dharshan.ks@wipro.com)
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/shipments")
public class TrackingUpdateController {

    /** Service layer for all tracking update business operations. */
    private final TrackingUpdateService trackingUpdateService;

    /**
     * Constructs a {@code TrackingUpdateController} with the required
     * {@link TrackingUpdateService}.
     *
     * @param trackingUpdateService the service bean responsible for tracking update business logic;
     *                              injected automatically by Spring's dependency injection container
     */
    public TrackingUpdateController(TrackingUpdateService trackingUpdateService) {
        this.trackingUpdateService = trackingUpdateService;
    }

    /**
     * Records a new tracking update for the specified shipment.
     *
     * <p>Accepts a validated {@link TrackingUpdateDto} request body and associates it with
     * the shipment identified by {@code shipmentId}. The update is persisted and the
     * resulting DTO (including the generated ID and timestamp) is returned in the response.
     * Only administrators should be authorised to post tracking updates.</p>
     *
     * @param shipmentId        the unique identifier of the shipment to which the tracking
     *                          update belongs; must refer to an existing shipment
     * @param trackingUpdateDto the data transfer object containing update details such as
     *                          location, status, and description; must pass all Bean Validation constraints
     * @return a {@link ResponseEntity} containing the saved {@link TrackingUpdateDto} with HTTP 200
     * @throws com.wip.exception.ResourceNotFoundException if no shipment exists with the given ID (HTTP 404)
     */
    @PostMapping("/{shipmentId}/tracking-updates")
    public ResponseEntity<TrackingUpdateDto> addTrackingUpdate(
            @PathVariable Long shipmentId,
            @Valid @RequestBody TrackingUpdateDto trackingUpdateDto) {
        return ResponseEntity.ok(trackingUpdateService.addTrackingUpdate(shipmentId, trackingUpdateDto));
    }

    /**
     * Retrieves all tracking updates recorded for a specific shipment.
     *
     * <p>Returns the complete chronological list of tracking update events associated with
     * the shipment identified by {@code shipmentId}. Each entry represents a discrete
     * status change or location checkpoint during the delivery journey. An empty list is
     * returned if no updates have been recorded yet.</p>
     *
     * @param shipmentId the unique identifier of the shipment whose tracking updates are requested
     * @return a {@link ResponseEntity} containing a {@link List} of {@link TrackingUpdateDto}
     *         objects ordered by their recorded timestamp, with HTTP 200
     * @throws com.wip.exception.ResourceNotFoundException if no shipment exists with the given ID (HTTP 404)
     */
    @GetMapping("/{shipmentId}/tracking-updates")
    public ResponseEntity<List<TrackingUpdateDto>> getTrackingUpdatesByShipmentId(
            @PathVariable Long shipmentId) {
        return ResponseEntity.ok(trackingUpdateService.getTrackingUpdatesByShipmentId(shipmentId));
    }
}
