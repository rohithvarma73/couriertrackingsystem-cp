package com.wip.controller;

import com.wip.dto.ParcelDto;
import com.wip.service.ParcelService;
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
@RequestMapping("/api/parcels")
@Tag(name = "Parcel Management", description = "APIs for parcel booking, retrieval, updates, and deletion")
public class ParcelController {

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @Operation(summary = "Create a new parcel", description = "Creates a parcel booking for an existing customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcel created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parcel data"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping("/addParcel")
    public ResponseEntity<ParcelDto> addParcel(@Valid @RequestBody ParcelDto parcelDto) {
        return ResponseEntity.ok(parcelService.addParcel(parcelDto));
    }

    @Operation(summary = "Get all parcels", description = "Returns all parcels in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcels retrieved successfully")
    })
    @GetMapping("/getAll")
    public ResponseEntity<List<ParcelDto>> getAllParcels() {
        return ResponseEntity.ok(parcelService.getAllParcels());
    }

    @Operation(summary = "Get parcel by ID", description = "Fetches a parcel using the parcel ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcel found"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ParcelDto> getParcelById(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(parcelService.getParcelById(id));
    }

    @Operation(summary = "Update parcel by ID", description = "Updates an existing parcel.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcel updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parcel data"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ParcelDto> updateParcel(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ParcelDto parcelDto) {
        return ResponseEntity.ok(parcelService.updateParcel(id, parcelDto));
    }

    @Operation(summary = "Delete parcel by ID", description = "Deletes a parcel using the parcel ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Parcel deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Parcel not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParcel(
            @Parameter(description = "Parcel ID", required = true)
            @PathVariable Long id) {
        parcelService.deleteParcel(id);
        return ResponseEntity.noContent().build();
    }
}