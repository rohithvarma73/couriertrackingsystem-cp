package com.wip.controller;

import com.wip.dto.ShipmentDto;
import com.wip.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

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
    @PostMapping
    public ResponseEntity<ShipmentDto> addShipment(@Valid @RequestBody ShipmentDto shipmentDto) {
        return ResponseEntity.ok(shipmentService.addShipment(shipmentDto));
    }

    @Operation(summary = "Get all shipments", description = "Returns all shipments in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipments retrieved successfully")
    })
    @GetMapping
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

    @Operation(summary = "Update shipment by ID", description = "Updates an existing shipment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipment updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid shipment data"),
            @ApiResponse(responseCode = "404", description = "Shipment not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ShipmentDto> updateShipment(
            @Parameter(description = "Shipment ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ShipmentDto shipmentDto) {
        return ResponseEntity.ok(shipmentService.updateShipment(id, shipmentDto));
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