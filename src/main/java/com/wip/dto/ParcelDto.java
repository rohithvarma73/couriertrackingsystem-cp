package com.wip.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for transferring parcel booking information between the API layer and the service layer.
 *
 * <p>This DTO carries the details required to create or update a parcel booking, including
 * the associated customer, physical parcel attributes (weight, addresses), and booking date.
 * Bean Validation annotations enforce data integrity at the API boundary. The
 * {@code shipmentId} and {@code shipmentAvailable} fields are populated by the server in
 * response payloads to indicate whether a shipment has been created for this parcel.</p>
 *
 * <p>For regular (non-admin) users, the {@code customerId} is automatically resolved from
 * the authenticated user's profile on the server side; admin users must supply it explicitly.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public class ParcelDto {

    /**
     * The unique identifier of the parcel. Populated by the server in response payloads.
     */
    private Long parcelId;

    /**
     * The ID of the customer to whom this parcel belongs.
     * Required for admin users when creating a parcel.
     * For regular users, this is always enforced server-side from the auth context.
     */
    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be a positive number")
    private Long customerId;

    /**
     * The full name of the customer, populated in response payloads for display purposes.
     */
    private String customerName;

    /**
     * The phone number of the parcel receiver, derived from the customer's phone on the server side.
     */
    private String receiverPhone;

    /**
     * The gross weight of the parcel in kilograms. Must be at least 0.1 kg.
     */
    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.1", message = "Weight must be at least 0.1 kg")
    private BigDecimal weight;

    /**
     * The pickup or origin address from which the parcel will be collected. Must not be blank.
     */
    @NotBlank(message = "Pickup address is required")
    private String sourceAddress;

    /**
     * The delivery destination address for this parcel. Must not be blank.
     */
    @NotBlank(message = "Delivery address is required")
    private String destinationAddress;

    /**
     * The date on which this parcel was booked. Must not be null.
     */
    @NotNull(message = "Booking date is required")
    private LocalDate bookingDate;

    /**
     * The ID of the associated shipment, if one has been created for this parcel.
     * Populated by the server in response payloads; {@code null} if no shipment exists.
     */
    private Long shipmentId;

    /**
     * Indicates whether a shipment record exists for this parcel.
     * {@code true} if a shipment has been created; {@code false} otherwise.
     */
    private boolean shipmentAvailable;

    /**
     * Returns the unique identifier of this parcel.
     *
     * @return the parcel ID, or {@code null} if not yet persisted
     */
    public Long getParcelId() {
        return parcelId;
    }

    /**
     * Sets the unique identifier of this parcel.
     *
     * @param parcelId the parcel ID to assign
     */
    public void setParcelId(Long parcelId) {
        this.parcelId = parcelId;
    }

    /**
     * Returns the ID of the customer associated with this parcel.
     *
     * @return the customer ID
     */
    public Long getCustomerId() {
        return customerId;
    }

    /**
     * Sets the ID of the customer associated with this parcel.
     *
     * @param customerId the customer ID to assign; must be a positive number
     */
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    /**
     * Returns the display name of the customer associated with this parcel.
     *
     * @return the customer's full name, or {@code null} if not populated
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Sets the display name of the customer associated with this parcel.
     *
     * @param customerName the customer name to assign
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * Returns the receiver's phone number for this parcel.
     *
     * @return the receiver phone number
     */
    public String getReceiverPhone() {
        return receiverPhone;
    }

    /**
     * Sets the receiver's phone number for this parcel.
     *
     * @param receiverPhone the receiver's phone number to assign
     */
    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    /**
     * Returns the weight of this parcel in kilograms.
     *
     * @return the parcel weight as a {@link BigDecimal}
     */
    public BigDecimal getWeight() {
        return weight;
    }

    /**
     * Sets the weight of this parcel in kilograms.
     *
     * @param weight the parcel weight to assign; must be at least 0.1
     */
    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    /**
     * Returns the source (pickup) address for this parcel.
     *
     * @return the origin address string
     */
    public String getSourceAddress() {
        return sourceAddress;
    }

    /**
     * Sets the source (pickup) address for this parcel.
     *
     * @param sourceAddress the origin address to assign; must not be blank
     */
    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    /**
     * Returns the destination (delivery) address for this parcel.
     *
     * @return the destination address string
     */
    public String getDestinationAddress() {
        return destinationAddress;
    }

    /**
     * Sets the destination (delivery) address for this parcel.
     *
     * @param destinationAddress the delivery address to assign; must not be blank
     */
    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    /**
     * Returns the booking date for this parcel.
     *
     * @return the booking date as a {@link LocalDate}
     */
    public LocalDate getBookingDate() {
        return bookingDate;
    }

    /**
     * Sets the booking date for this parcel.
     *
     * @param bookingDate the booking date to assign; must not be null
     */
    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    /**
     * Returns the ID of the shipment associated with this parcel.
     *
     * @return the shipment ID, or {@code null} if no shipment has been created
     */
    public Long getShipmentId() {
        return shipmentId;
    }

    /**
     * Sets the ID of the shipment associated with this parcel.
     *
     * @param shipmentId the shipment ID to assign
     */
    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    /**
     * Returns whether a shipment is available for this parcel.
     *
     * @return {@code true} if a shipment record exists; {@code false} otherwise
     */
    public boolean isShipmentAvailable() {
        return shipmentAvailable;
    }

    /**
     * Sets the shipment availability flag for this parcel.
     *
     * @param shipmentAvailable {@code true} if a shipment exists; {@code false} otherwise
     */
    public void setShipmentAvailable(boolean shipmentAvailable) {
        this.shipmentAvailable = shipmentAvailable;
    }
}
