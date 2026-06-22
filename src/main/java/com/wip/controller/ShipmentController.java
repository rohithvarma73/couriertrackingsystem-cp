package com.wip.controller;

import com.wip.dto.ShipmentDto;
import com.wip.service.ShipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * REST controller for managing shipments in the Courier Tracking System.
 *
 * <p>Exposes RESTful endpoints under {@code /api/shipments} that allow authorised
 * clients to create, retrieve, update, and delete shipments. A shipment is generated
 * for an existing parcel and is assigned a unique tracking number upon creation.
 * All responses are serialised as JSON and the API is documented via SpringDoc / OpenAPI
 * annotations. This controller shares the {@code /api/shipments} base path with
 * {@link TrackingUpdateController}, which handles tracking-update sub-resources.</p>
 *
 * @author Dharshan K S (dharshan.ks@wipro.com)
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/shipments")
@Tag(name = "Shipment Management", description = "APIs for shipment creation, tracking, updates, and deletion")
public class ShipmentController {

    /** Service layer for all shipment business operations. */
    private final ShipmentService shipmentService;

    /**
     * Constructs a {@code ShipmentController} with the required {@link ShipmentService}.
     *
     * @param shipmentService the service bean responsible for shipment business logic;
     *                        injected automatically by Spring's dependency injection container
     */
    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    /**
     * Creates a new shipment for an existing parcel.
     *
     * <p>Initiates a shipment record for the parcel identified by {@code parcelid}. The
     * service layer generates a unique tracking number and sets the initial shipment status.
     * Only one shipment may exist per parcel; attempting to create a duplicate results in
     * an {@link IllegalStateException} from the service layer.</p>
     *
     * @param parcelid the unique identifier of the parcel for which the shipment is being created;
     *                 must refer to an existing, un-shipped parcel
     * @return a {@link ResponseEntity} containing the created {@link ShipmentDto} with HTTP 200
     * @throws com.wip.exception.ResourceNotFoundException if no parcel exists with the given ID (HTTP 404)
     * @throws IllegalStateException if a shipment already exists for the given parcel
     */
    @Operation(summary = "Create a new shipment", description = "Creates a shipment for an existing parcel.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid shipment data"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    @PostMapping("/addShipment/{parcelid}")
    public ResponseEntity<ShipmentDto> addShipment(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable Long parcelid) {
        return ResponseEntity.ok(shipmentService.addShipment(parcelid));
    }

    /**
     * Retrieves a list of all shipments currently registered in the system.
     *
     * <p>Returns every shipment record in the database, each mapped to a {@link ShipmentDto}.
     * An empty list is returned when no shipments exist.</p>
     *
     * @return a {@link ResponseEntity} containing a {@link List} of {@link ShipmentDto} objects
     *         representing all shipments, with HTTP 200
     */
    @Operation(summary = "Get all shipments", description = "Returns all shipments in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipments retrieved successfully")
    })
    @GetMapping("/getAll")
    public ResponseEntity<List<ShipmentDto>> getAllShipments() {
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }

    /**
     * Retrieves the details of a single shipment identified by its unique ID.
     *
     * <p>Looks up the shipment with the given {@code id}. If no record is found, the
     * service layer throws a {@code ResourceNotFoundException}, resulting in an HTTP 404.</p>
     *
     * @param id the unique identifier of the shipment to retrieve; must be a positive {@link Long}
     * @return a {@link ResponseEntity} containing the matching {@link ShipmentDto} with HTTP 200
     * @throws com.wip.exception.ResourceNotFoundException if no shipment exists with the given ID (HTTP 404)
     */
    @Operation(summary = "Get shipment by ID", description = "Fetches a shipment using the shipment ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipment found"),
            @ApiResponse(responseCode = "404", description = "Shipment not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDto> getShipmentById(
            @Parameter(description = "Shipment ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getShipmentById(id));
    }

    /**
     * Retrieves a shipment using its unique tracking number.
     *
     * <p>Provides a public-facing lookup endpoint that allows users and external systems
     * to fetch shipment details by supplying the tracking number that was generated at
     * shipment creation time.</p>
     *
     * @param trackingNumber the unique alphanumeric tracking number assigned to the shipment
     * @return a {@link ResponseEntity} containing the matching {@link ShipmentDto} with HTTP 200
     * @throws com.wip.exception.ResourceNotFoundException if no shipment exists with the given
     *         tracking number (HTTP 404)
     */
    @Operation(summary = "Get shipment by tracking number", description = "Fetches a shipment using the tracking number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipment found"),
            @ApiResponse(responseCode = "404", description = "Shipment not found")
    })
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ShipmentDto> getShipmentByTrackingNumber(
            @Parameter(description = "Tracking number", required = true)
            @PathVariable String trackingNumber) {
        return ResponseEntity.ok(shipmentService.getShipmentByTrackingNumber(trackingNumber));
    }

    /**
     * Updates the current geographic location of a shipment in transit.
     *
     * <p>Accepts a plain-text {@code currentLocation} request parameter and persists it
     * against the shipment identified by {@code id}. This endpoint is intended for
     * lightweight location-only updates, as opposed to the full-update operation.</p>
     *
     * @param id              the unique identifier of the shipment whose location is to be updated
     * @param currentLocation the new current location string (e.g. city name or hub identifier)
     * @return a {@link ResponseEntity} containing the updated {@link ShipmentDto} with HTTP 200
     * @throws com.wip.exception.ResourceNotFoundException if no shipment exists with the given ID (HTTP 404)
     */
    @Operation(summary = "Update shipment location", description = "Updates only the current location of a shipment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipment location updated successfully"),
            @ApiResponse(responseCode = "404", description = "Shipment not found")
    })
    @PutMapping("/{id}/location")
    public ResponseEntity<ShipmentDto> updateShipmentLocation(
            @Parameter(description = "Shipment ID", required = true)
            @PathVariable Long id,
            @RequestParam String currentLocation) {
        return ResponseEntity.ok(shipmentService.updateShipmentLocation(id, currentLocation));
    }

    /**
     * Permanently deletes the shipment identified by the given ID.
     *
     * <p>Removes the shipment record from the system along with any associated tracking
     * updates (cascaded by the service layer). On successful deletion, a
     * {@code 204 No Content} response is returned with an empty body.</p>
     *
     * @param id the unique identifier of the shipment to delete; must be a positive {@link Long}
     * @return a {@link ResponseEntity} with no body and HTTP 204 on success
     * @throws com.wip.exception.ResourceNotFoundException if no shipment exists with the given ID (HTTP 404)
     */
    @Operation(summary = "Delete shipment by ID", description = "Deletes a shipment using the shipment ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Shipment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Shipment not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(
            @Parameter(description = "Shipment ID", required = true)
            @PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }
}
