package com.wip.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * JPA entity representing a shipment dispatched for a parcel in the courier tracking system.
 *
 * <p>Mapped to the {@code shipment} database table, this entity records the logistics
 * details of a dispatched parcel, including a system-generated unique tracking number,
 * the shipment dispatch date, current geographic location, and the expected delivery date.
 * Each shipment is tied to exactly one {@link Parcel} and may accumulate multiple
 * {@link TrackingUpdate} entries over its lifecycle. Cascade ALL and orphan removal on
 * tracking updates ensure consistency when a shipment is deleted.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "shipment")
public class Shipment {

    /**
     * Unique auto-generated surrogate primary key for the shipment record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shipmentId;

    /**
     * System-generated unique tracking number used to publicly identify and track
     * this shipment. Must not be null and must be unique across all shipments.
     */
    @Column(unique = true, nullable = false)
    private String trackingNumber;

    /**
     * The date on which this shipment was dispatched.
     */
    private LocalDate shipmentDate;

    /**
     * The current geographic location of the shipment during transit.
     * Updated as the parcel moves through distribution hubs.
     */
    private String currentLocation;

    /**
     * The estimated date by which the parcel is expected to be delivered.
     */
    private LocalDate estimatedDeliveryDate;

    /**
     * The {@link Parcel} associated with this shipment. References the
     * {@code parcel} table via the {@code parcel_id} foreign key column.
     */
    @OneToOne
    @JoinColumn(name = "parcel_id")
    private Parcel parcel;

    /**
     * The {@link AppUser} who created this shipment record. Loaded lazily;
     * references the {@code users} table via the {@code created_by_user_id}
     * foreign key column. Must not be null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private AppUser createdBy;

    /**
     * Ordered list of {@link TrackingUpdate} events recorded for this shipment.
     * Mapped by the {@code shipment} field in the {@code TrackingUpdate} entity.
     * Cascade ALL and orphan removal ensure updates are removed with the shipment.
     */
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrackingUpdate> trackingUpdates;

    /**
     * Returns the unique identifier of this shipment.
     *
     * @return the auto-generated shipment ID
     */
    public Long getShipmentId() { return shipmentId; }

    /**
     * Sets the unique identifier of this shipment.
     *
     * @param shipmentId the shipment ID to assign
     */
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }

    /**
     * Returns the unique tracking number for this shipment.
     *
     * @return the tracking number string
     */
    public String getTrackingNumber() { return trackingNumber; }

    /**
     * Sets the unique tracking number for this shipment.
     *
     * @param trackingNumber the tracking number to assign
     */
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    /**
     * Returns the dispatch date of this shipment.
     *
     * @return the shipment date as a {@link LocalDate}
     */
    public LocalDate getShipmentDate() { return shipmentDate; }

    /**
     * Sets the dispatch date of this shipment.
     *
     * @param shipmentDate the dispatch date to assign
     */
    public void setShipmentDate(LocalDate shipmentDate) { this.shipmentDate = shipmentDate; }

    /**
     * Returns the current geographic location of this shipment.
     *
     * @return a string describing the current location during transit
     */
    public String getCurrentLocation() { return currentLocation; }

    /**
     * Sets the current geographic location of this shipment.
     *
     * @param currentLocation the current location string to assign
     */
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }

    /**
     * Returns the estimated delivery date for this shipment.
     *
     * @return the estimated delivery date as a {@link LocalDate}
     */
    public LocalDate getEstimatedDeliveryDate() { return estimatedDeliveryDate; }

    /**
     * Sets the estimated delivery date for this shipment.
     *
     * @param estimatedDeliveryDate the estimated delivery date to assign
     */
    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }

    /**
     * Returns the {@link Parcel} linked to this shipment.
     *
     * @return the associated parcel, or {@code null} if not set
     */
    public Parcel getParcel() { return parcel; }

    /**
     * Sets the {@link Parcel} linked to this shipment.
     *
     * @param parcel the parcel to associate with this shipment
     */
    public void setParcel(Parcel parcel) { this.parcel = parcel; }

    /**
     * Returns the {@link AppUser} who created this shipment record.
     *
     * @return the user who created this shipment, or {@code null} if not set
     */
    public AppUser getCreatedBy() { return createdBy; }

    /**
     * Sets the {@link AppUser} who created this shipment record.
     *
     * @param createdBy the user responsible for creating this shipment
     */
    public void setCreatedBy(AppUser createdBy) { this.createdBy = createdBy; }

    /**
     * Returns the list of {@link TrackingUpdate} events for this shipment.
     *
     * @return an ordered list of tracking update records, may be empty
     */
    public List<TrackingUpdate> getTrackingUpdates() { return trackingUpdates; }

    /**
     * Sets the list of {@link TrackingUpdate} events for this shipment.
     *
     * @param trackingUpdates the list of tracking updates to associate
     */
    public void setTrackingUpdates(List<TrackingUpdate> trackingUpdates) { this.trackingUpdates = trackingUpdates; }
}
