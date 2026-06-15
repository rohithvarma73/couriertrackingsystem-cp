package com.wip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ShipmentDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Shipment ID")
    private Long shipmentId;

    @Schema(example = "TRK1001", description = "Tracking number")
    @NotBlank(message = "Tracking number is required")
    private String trackingNumber;

    @Schema(example = "2026-06-12", description = "Shipment date")
    @NotNull(message = "Shipment date is required")
    private LocalDate shipmentDate;

    @Schema(example = "Delhi Hub", description = "Current location")
    @NotBlank(message = "Current location is required")
    private String currentLocation;

    @Schema(example = "2026-06-15", description = "Estimated delivery date")
    @NotNull(message = "Estimated delivery date is required")
    private LocalDate estimatedDeliveryDate;

    @Schema(example = "1", description = "Parcel ID")
    @NotNull(message = "Parcel ID is required")
    private Long parcelId;

	public Long getShipmentId() {
		return shipmentId;
	}

	public void setShipmentId(Long shipmentId) {
		this.shipmentId = shipmentId;
	}

	public String getTrackingNumber() {
		return trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}

	public LocalDate getShipmentDate() {
		return shipmentDate;
	}

	public void setShipmentDate(LocalDate shipmentDate) {
		this.shipmentDate = shipmentDate;
	}

	public String getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(String currentLocation) {
		this.currentLocation = currentLocation;
	}

	public LocalDate getEstimatedDeliveryDate() {
		return estimatedDeliveryDate;
	}

	public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
		this.estimatedDeliveryDate = estimatedDeliveryDate;
	}

	public Long getParcelId() {
		return parcelId;
	}

	public void setParcelId(Long parcelId) {
		this.parcelId = parcelId;
	}

}