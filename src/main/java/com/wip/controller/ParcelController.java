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

/**
 * REST controller for managing parcel bookings in the Courier Tracking System.
 *
 * <p>Exposes RESTful endpoints under {@code /api/parcels} for full CRUD operations on
 * parcel resources. A parcel represents a physical item booked for delivery by a registered
 * customer. All responses are serialised as JSON. Bean Validation is enforced on incoming
 * request payloads, and OpenAPI documentation is generated via SpringDoc annotations.</p>
 *
 * @author Dharshan K S (dharshan.ks@wipro.com)
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/parcels")
@Tag(name = "Parcel Management", description = "APIs for parcel booking, retrieval, updates, and deletion")
public class ParcelController {

    /** Service layer for all parcel business operations. */
    private final ParcelService parcelService;

    /**
     * Constructs a {@code ParcelController} with the required {@link ParcelService}.
     *
     * @param parcelService the service bean responsible for parcel business logic;
     *                      injected automatically by Spring's dependency injection container
     */
    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    /**
     * Creates a new parcel booking for an existing customer.
     *
     * <p>Accepts a validated {@link ParcelDto} request body containing details such as
     * weight, dimensions, destination address, and the owning customer's ID. The persisted
     * parcel (including its generated ID) is returned in the response body.</p>
     *
     * @param parcelDto the data transfer object containing parcel booking details;
     *                  must pass all Bean Validation constraints
     * @return a {@link ResponseEntity} containing the saved {@link ParcelDto} with HTTP 200
     * @throws com.wip.exception.ResourceNotFoundException if the referenced customer does not exist (HTTP 404)
     */
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

    /**
     * Retrieves a list of all parcel bookings registered in the system.
     *
     * <p>Returns every parcel record in the database mapped to a {@link ParcelDto}.
     * An empty list is returned when no parcels exist.</p>
     *
     * @return a {@link ResponseEntity} containing a {@link List} of {@link ParcelDto} objects
     *         representing all parcels, with HTTP 200
     */
    @Operation(summary = "Get all parcels", description = "Returns all parcels in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parcels retrieved successfully")
    })
    @GetMapping("/getAll")
    public ResponseEntity<List<ParcelDto>> getAllParcels() {
        return ResponseEntity.ok(parcelService.getAllParcels());
    }

    /**
     * Retrieves the details of a single parcel identified by its unique ID.
     *
     * <p>Looks up the parcel with the given {@code id}. If no record is found, the service
     * layer throws a {@code ResourceNotFoundException}, resulting in an HTTP 404 response.</p>
     *
     * @param id the unique identifier of the parcel to retrieve; must be a positive {@link Long}
     * @return a {@link ResponseEntity} containing the matching {@link ParcelDto} with HTTP 200
     * @throws com.wip.exception.ResourceNotFoundException if no parcel exists with the given ID (HTTP 404)
     */
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

    /**
     * Updates an existing parcel booking with new data.
     *
     * <p>Applies the fields from the supplied {@link ParcelDto} to the parcel identified
     * by {@code id}. The parcel must already exist; otherwise a {@code ResourceNotFoundException}
     * is thrown. Bean Validation is enforced on the request body before processing.</p>
     *
     * @param id        the unique identifier of the parcel to update
     * @param parcelDto the data transfer object containing updated parcel details;
     *                  must pass all Bean Validation constraints
     * @return a {@link ResponseEntity} containing the updated {@link ParcelDto} with HTTP 200
     * @throws com.wip.exception.ResourceNotFoundException if no parcel exists with the given ID (HTTP 404)
     */
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

    /**
     * Permanently deletes the parcel booking identified by the given ID.
     *
     * <p>Removes the parcel record from the system. On successful deletion, a
     * {@code 204 No Content} response is returned with an empty body. If the parcel
     * does not exist, the service layer throws a {@code ResourceNotFoundException}.</p>
     *
     * @param id the unique identifier of the parcel to delete; must be a positive {@link Long}
     * @return a {@link ResponseEntity} with no body and HTTP 204 on success
     * @throws com.wip.exception.ResourceNotFoundException if no parcel exists with the given ID (HTTP 404)
     */
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
