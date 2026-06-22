package com.wip.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.*;

/**
 * JPA entity representing a courier parcel booking in the tracking system.
 *
 * <p>Mapped to the {@code parcel} database table, this entity stores all details
 * pertaining to a parcel shipment request, including source and destination
 * addresses, weight, booking date, the associated {@link Customer}, and a
 * one-to-one reference to the {@link Shipment} created for this parcel. Cascade
 * removal on the shipment relationship ensures orphaned shipment records are
 * automatically cleaned up when a parcel is deleted.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "parcel")
public class Parcel {

    /**
     * Unique auto-generated surrogate primary key for the parcel record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long parcelId;

    /**
     * Phone number of the parcel receiver, used for delivery notifications.
     */
    private String receiverPhone;

    /**
     * Gross weight of the parcel in kilograms, stored with decimal precision.
     */
    private BigDecimal weight;

    /**
     * The pickup or origin address from which the parcel is collected.
     */
    private String sourceAddress;

    /**
     * The delivery destination address for this parcel.
     */
    private String destinationAddress;

    /**
     * The date on which this parcel booking was made.
     */
    private LocalDate bookingDate;

    /**
     * The {@link Customer} who owns this parcel booking. References the
     * {@code customer} table via the {@code customer_id} foreign key column.
     */
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    /**
     * The {@link AppUser} who created this parcel record. Loaded lazily;
     * references the {@code users} table via the {@code created_by_user_id}
     * foreign key column. Must not be null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private AppUser createdBy;

    /**
     * The {@link Shipment} dispatched for this parcel. The relationship is mapped
     * by the {@code parcel} field in the {@code Shipment} entity. Cascade ALL and
     * orphan removal ensure the shipment lifecycle is tied to this parcel.
     */
    @OneToOne(mappedBy = "parcel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Shipment shipment;

    /**
     * Default no-argument constructor required by the JPA specification.
     */
    public Parcel() {
    }

    /**
     * Returns the unique identifier of this parcel.
     *
     * @return the auto-generated parcel ID
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
     * @param weight the parcel weight to assign
     */
    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    /**
     * Returns the source (pickup) address for this parcel.
     *
     * @return the source address string
     */
    public String getSourceAddress() {
        return sourceAddress;
    }

    /**
     * Sets the source (pickup) address for this parcel.
     *
     * @param sourceAddress the origin address to assign
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
     * @param destinationAddress the delivery address to assign
     */
    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    /**
     * Returns the date on which this parcel was booked.
     *
     * @return the booking date as a {@link LocalDate}
     */
    public LocalDate getBookingDate() {
        return bookingDate;
    }

    /**
     * Sets the date on which this parcel was booked.
     *
     * @param bookingDate the booking date to assign
     */
    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    /**
     * Returns the {@link Customer} associated with this parcel booking.
     *
     * @return the owning customer, or {@code null} if not set
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Sets the {@link Customer} associated with this parcel booking.
     *
     * @param customer the customer to link to this parcel
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    /**
     * Returns the {@link AppUser} who created this parcel record.
     *
     * @return the user who created this parcel, or {@code null} if not set
     */
    public AppUser getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the {@link AppUser} who created this parcel record.
     *
     * @param createdBy the user responsible for creating this parcel
     */
    public void setCreatedBy(AppUser createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Returns the {@link Shipment} dispatched for this parcel.
     *
     * @return the associated shipment, or {@code null} if no shipment exists yet
     */
    public Shipment getShipment() {
        return shipment;
    }

    /**
     * Sets the {@link Shipment} dispatched for this parcel.
     *
     * @param shipment the shipment to associate with this parcel
     */
    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }
}
