package com.wip.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a single tracking event for a shipment in the courier tracking system.
 *
 * <p>Mapped to the {@code tracking_update} database table, each record captures a
 * timestamped checkpoint in a parcel's journey, including the current delivery status
 * (e.g., "In Transit", "Delivered"), the geographic location at the time of the update,
 * and optional remarks entered by the administrator. Multiple tracking updates may be
 * associated with a single {@link Shipment}, forming an audit trail of the shipment's
 * progress from pickup to delivery.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "tracking_update")
public class TrackingUpdate {

    /**
     * Unique auto-generated surrogate primary key for the tracking update record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long updateId;

    /**
     * Human-readable delivery status at the time of this tracking event
     * (e.g., {@code "Picked Up"}, {@code "In Transit"}, {@code "Out for Delivery"},
     * {@code "Delivered"}).
     */
    private String deliveryStatus;

    /**
     * Geographic location of the shipment at the time this tracking update was recorded.
     */
    private String location;

    /**
     * Optional free-text remarks or notes added by the administrator for this event.
     */
    private String remarks;

    /**
     * The date and time at which this tracking update was created, recorded in
     * system local time.
     */
    private LocalDateTime createdAt;

    /**
     * The {@link Shipment} to which this tracking update belongs. References the
     * {@code shipment} table via the {@code shipment_id} foreign key column.
     * Must not be null.
     */
    @ManyToOne
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    /**
     * The {@link AppUser} who recorded this tracking update. Loaded lazily;
     * references the {@code users} table via the {@code created_by_user_id}
     * foreign key column. Must not be null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private AppUser createdBy;

    /**
     * Returns the unique identifier of this tracking update.
     *
     * @return the auto-generated update ID
     */
    public Long getUpdateId() { return updateId; }

    /**
     * Sets the unique identifier of this tracking update.
     *
     * @param updateId the update ID to assign
     */
    public void setUpdateId(Long updateId) { this.updateId = updateId; }

    /**
     * Returns the delivery status recorded for this tracking event.
     *
     * @return the delivery status string (e.g., {@code "In Transit"})
     */
    public String getDeliveryStatus() { return deliveryStatus; }

    /**
     * Sets the delivery status for this tracking event.
     *
     * @param deliveryStatus the delivery status string to assign
     */
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    /**
     * Returns the geographic location recorded for this tracking event.
     *
     * @return the location string at the time of this update
     */
    public String getLocation() { return location; }

    /**
     * Sets the geographic location for this tracking event.
     *
     * @param location the location string to assign
     */
    public void setLocation(String location) { this.location = location; }

    /**
     * Returns the optional remarks or notes for this tracking event.
     *
     * @return the remarks string, or {@code null} if no remarks were provided
     */
    public String getRemarks() { return remarks; }

    /**
     * Sets the optional remarks or notes for this tracking event.
     *
     * @param remarks the remarks text to assign
     */
    public void setRemarks(String remarks) { this.remarks = remarks; }

    /**
     * Returns the timestamp at which this tracking update was recorded.
     *
     * @return the creation timestamp as a {@link LocalDateTime}
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * Sets the timestamp at which this tracking update was recorded.
     *
     * @param createdAt the creation timestamp to assign
     */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Returns the {@link Shipment} associated with this tracking update.
     *
     * @return the parent shipment record
     */
    public Shipment getShipment() { return shipment; }

    /**
     * Sets the {@link Shipment} associated with this tracking update.
     *
     * @param shipment the shipment to link this update to
     */
    public void setShipment(Shipment shipment) { this.shipment = shipment; }

    /**
     * Returns the {@link AppUser} who recorded this tracking update.
     *
     * @return the user who created this update, or {@code null} if not set
     */
    public AppUser getCreatedBy() { return createdBy; }

    /**
     * Sets the {@link AppUser} who recorded this tracking update.
     *
     * @param createdBy the user responsible for creating this update
     */
    public void setCreatedBy(AppUser createdBy) { this.createdBy = createdBy; }
}
