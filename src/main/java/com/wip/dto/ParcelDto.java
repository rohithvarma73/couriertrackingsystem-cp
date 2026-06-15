package com.wip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ParcelDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Parcel ID")
    private Long parcelId;

    @Schema(example = "9999678901", description = "Receiver phone number")
    @NotBlank(message = "Receiver phone is required")
    private String receiverPhone;

    @Schema(example = "2.5", description = "Parcel weight")
    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be greater than 0")
    private BigDecimal weight;

    @Schema(example = "Delhi", description = "Source address")
    @NotBlank(message = "Source address is required")
    private String sourceAddress;

    @Schema(example = "Mumbai", description = "Destination address")
    @NotBlank(message = "Destination address is required")
    private String destinationAddress;

    @Schema(example = "2026-06-12", description = "Booking date")
    @NotNull(message = "Booking date is required")
    private LocalDate bookingDate;

    @Schema(example = "1", description = "Customer ID")
    @NotNull(message = "Customer ID is required")
    private Long customerId;

	public Long getParcelId() {
		return parcelId;
	}

	public void setParcelId(Long parcelId) {
		this.parcelId = parcelId;
	}

	public String getReceiverPhone() {
		return receiverPhone;
	}

	public void setReceiverPhone(String receiverPhone) {
		this.receiverPhone = receiverPhone;
	}

	public BigDecimal getWeight() {
		return weight;
	}

	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}

	public String getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public String getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(String destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public LocalDate getBookingDate() {
		return bookingDate;
	}

	public void setBookingDate(LocalDate bookingDate) {
		this.bookingDate = bookingDate;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

    
}