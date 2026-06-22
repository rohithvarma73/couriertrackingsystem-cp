package com.wip.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for transferring tracking update information between the API layer and the service layer.
 *
 * <p>This DTO represents a single checkpoint event in a shipment's delivery journey.
 * It is used both as a request body (when an administrator records a new tracking
 * milestone) and as a response payload (to display the history of delivery events).
 * The {@code updateId}, {@code shipmentId}, {@code trackingNumber}, and {@code createdAt}
 * fields are populated by the server in response payloads. The {@code deliveryStatus}
 * and {@code location} fields are mandatory for create requests.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public class TrackingUpdateDto {

    /**
     * The unique identifier of this tracking update record. Populated by the server
     * in response payloads.
     */
    private Long updateId;

    /**
     * The ID of the shipment to which this tracking update belongs. Populated by the
     * server in response payloads.
     */
    private Long shipmentId;

    /**
     * The tracking number of the associated shipment. Populated by the server in
     * response payloads for display convenience.
     */
    private String trackingNumber;

    /**
     * The delivery status recorded at the time of this tracking event
     * (e.g., {@code "Picked Up"}, {@code "In Transit"}, {@code "Out for Delivery"},
     * {@code "Delivered"}). Must not be blank.
     */
    @NotBlank(message = "Delivery status is required")
    private String deliveryStatus;

    /**
     * The geographic location of the shipment at the time this tracking update was
     * recorded. Must not be blank.
     */
    @NotBlank(message = "Location is required")
    private String location;

    /**
     * Optional free-text remarks or notes provided by the administrator for this event.
     * May be {@code null} or empty.
     */
    private String remarks;

    /**
     * The date and time at which this tracking update was created. Populated by the
     * server; read-only in client requests.
     */
    private LocalDateTime createdAt;

    /**
     * Returns the unique identifier of this tracking update.
     *
     * @return the update ID, or {@code null} if not yet persisted
     */
    public Long getUpdateId() {
        return updateId;
    }

    /**
     * Sets the unique identifier of this tracking update.
     *
     * @param updateId the update ID to assign
     */
    public void setUpdateId(Long updateId) {
        this.updateId = updateId;
    }

    /**
     * Returns the ID of the shipment associated with this tracking update.
     *
     * @return the shipment ID
     */
    public Long getShipmentId() {
        return shipmentId;
    }

    /**
     * Sets the ID of the shipment associated with this tracking update.
     *
     * @param shipmentId the shipment ID to assign
     */
    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    /**
     * Returns the tracking number of the associated shipment.
     *
     * @return the tracking number string, or {@code null} if not populated
     */
    public String getTrackingNumber() {
        return trackingNumber;
    }

    /**
     * Sets the tracking number of the associated shipment.
     *
     * @param trackingNumber the tracking number to assign
     */
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    /**
     * Returns the delivery status recorded for this tracking event.
     *
     * @return the delivery status string (e.g., {@code "In Transit"})
     */
    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    /**
     * Sets the delivery status for this tracking event.
     *
     * @param deliveryStatus the delivery status to assign; must not be blank
     */
    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    /**
     * Returns the geographic location recorded for this tracking event.
     *
     * @return the location string at the time of this update
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the geographic location for this tracking event.
     *
     * @param location the location string to assign; must not be blank
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the optional remarks or notes for this tracking event.
     *
     * @return the remarks string, or {@code null} if none were provided
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * Sets the optional remarks or notes for this tracking event.
     *
     * @param remarks the remarks text to assign
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Returns the timestamp at which this tracking update was created.
     *
     * @return the creation timestamp as a {@link LocalDateTime}
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp at which this tracking update was created.
     *
     * @param createdAt the creation timestamp to assign
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
