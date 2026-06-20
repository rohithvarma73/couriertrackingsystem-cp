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
 * ShipmentController Component.
 * 
 * Handles operations and data related to ShipmentController.
 */
@RestController
@RequestMapping("/api/shipments")
@Tag(name = "Shipment Management", description = "APIs for shipment creation, tracking, updates, and deletion")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

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

    @Operation(summary = "Get all shipments", description = "Returns all shipments in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipments retrieved successfully")
    })
    @GetMapping("/getAll")
    public ResponseEntity<List<ShipmentDto>> getAllShipments() {
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }

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
