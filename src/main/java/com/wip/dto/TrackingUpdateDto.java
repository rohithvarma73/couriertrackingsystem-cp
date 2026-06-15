package com.wip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class TrackingUpdateDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Tracking update ID")
    private Long updateId;

    @Schema(example = "In Transit", description = "Delivery status")
    @NotBlank(message = "Delivery status is required")
    private String deliveryStatus;

    @Schema(example = "Jaipur Hub", description = "Current location")
    @NotBlank(message = "Location is required")
    private String location;

    @Schema(example = "Parcel reached sorting center", description = "Remarks")
    @NotBlank(message = "Remarks are required")
    private String remarks;

    @Schema(example = "2026-06-12T18:00:00", description = "Updated time")
    private LocalDateTime updatedTime;

    @Schema(example = "1", description = "Shipment ID")
    @NotNull(message = "Shipment ID is required")
    private Long shipmentId;

	public Long getUpdateId() {
		return updateId;
	}

	public void setUpdateId(Long updateId) {
		this.updateId = updateId;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public LocalDateTime getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(LocalDateTime updatedTime) {
		this.updatedTime = updatedTime;
	}

	public Long getShipmentId() {
		return shipmentId;
	}

	public void setShipmentId(Long shipmentId) {
		this.shipmentId = shipmentId;
	}

}