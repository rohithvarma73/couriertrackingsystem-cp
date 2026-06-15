package com.wip.controller;

import com.wip.dto.TrackingUpdateDto;
import com.wip.service.TrackingUpdateService;
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
@RequestMapping("/api/tracking-updates")
@Tag(name = "Tracking Updates", description = "APIs for tracking updates and shipment status history")
public class TrackingUpdateController {

    private final TrackingUpdateService trackingUpdateService;

    public TrackingUpdateController(TrackingUpdateService trackingUpdateService) {
        this.trackingUpdateService = trackingUpdateService;
    }

    @Operation(summary = "Create a tracking update", description = "Adds a new tracking update for a shipment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tracking update created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid tracking update data"),
            @ApiResponse(responseCode = "404", description = "Shipment not found")
    })
    @PostMapping
    public ResponseEntity<TrackingUpdateDto> addTrackingUpdate(@Valid @RequestBody TrackingUpdateDto trackingUpdateDto) {
        return ResponseEntity.ok(trackingUpdateService.addTrackingUpdate(trackingUpdateDto));
    }

    @Operation(summary = "Get all tracking updates", description = "Returns all tracking updates.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tracking updates retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<TrackingUpdateDto>> getAllTrackingUpdates() {
        return ResponseEntity.ok(trackingUpdateService.getAllTrackingUpdates());
    }

    @Operation(summary = "Get tracking update by ID", description = "Fetches a tracking update using the update ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tracking update found"),
            @ApiResponse(responseCode = "404", description = "Tracking update not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TrackingUpdateDto> getTrackingUpdateById(
            @Parameter(description = "Tracking update ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(trackingUpdateService.getTrackingUpdateById(id));
    }

    @Operation(summary = "Update tracking update by ID", description = "Updates an existing tracking update.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tracking update updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid tracking update data"),
            @ApiResponse(responseCode = "404", description = "Tracking update not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TrackingUpdateDto> updateTrackingUpdate(
            @Parameter(description = "Tracking update ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody TrackingUpdateDto trackingUpdateDto) {
        return ResponseEntity.ok(trackingUpdateService.updateTrackingUpdate(id, trackingUpdateDto));
    }

    @Operation(summary = "Delete tracking update by ID", description = "Deletes a tracking update using the update ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tracking update deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Tracking update not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrackingUpdate(
            @Parameter(description = "Tracking update ID", required = true)
            @PathVariable Long id) {
        trackingUpdateService.deleteTrackingUpdate(id);
        return ResponseEntity.noContent().build();
    }
}