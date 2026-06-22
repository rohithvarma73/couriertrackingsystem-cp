package com.wip.entity;

import jakarta.persistence.*;
import java.util.List;

/**
 * JPA entity representing a customer in the courier tracking system.
 *
 * <p>Mapped to the {@code customer} database table, this entity captures the
 * personal and contact details of a customer. Each customer record is associated
 * with the {@link AppUser} who created it ({@code createdBy}), maintains a
 * one-to-one back-reference to that same user ({@code appUser}), and owns a
 * collection of {@link Parcel} bookings made under this customer profile.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "customer")
public class Customer {

    /**
     * Unique auto-generated surrogate primary key for the customer record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    /**
     * Full name of the customer.
     */
    private String customerName;

    /**
     * Email address of the customer used for correspondence.
     */
    private String email;

    /**
     * Contact phone number of the customer.
     */
    private String phone;

    /**
     * Residential or business address of the customer.
     */
    private String address;

    /**
     * The {@link AppUser} who created this customer record. Loaded lazily to
     * avoid unnecessary joins; references the {@code users} table via the
     * {@code created_by_user_id} foreign key column. Must not be null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private AppUser createdBy;

    /**
     * The list of {@link Parcel} bookings associated with this customer.
     * Mapped by the {@code customer} field in the {@code Parcel} entity.
     */
    @OneToMany(mappedBy = "customer")
    private List<Parcel> parcels;

    /**
     * The {@link AppUser} whose account is directly linked to this customer
     * profile via a one-to-one inverse relationship. Mapped by the {@code customer}
     * field in {@code AppUser}.
     */
    @OneToOne(mappedBy = "customer")
    private AppUser appUser;

    /**
     * Default no-argument constructor required by the JPA specification.
     */
    public Customer() {
    }

    /**
     * Returns the unique identifier of this customer.
     *
     * @return the auto-generated customer ID
     */
    public Long getCustomerId() {
        return customerId;
    }

    /**
     * Sets the unique identifier of this customer.
     *
     * @param customerId the customer ID to assign
     */
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    /**
     * Returns the full name of this customer.
     *
     * @return the customer's full name
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Sets the full name of this customer.
     *
     * @param customerName the full name to assign
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * Returns the email address of this customer.
     *
     * @return the customer's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of this customer.
     *
     * @param email the email address to assign
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the contact phone number of this customer.
     *
     * @return the customer's phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the contact phone number of this customer.
     *
     * @param phone the phone number to assign
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the address of this customer.
     *
     * @return the customer's address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address of this customer.
     *
     * @param address the address to assign
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns the {@link AppUser} who created this customer record.
     *
     * @return the user who created this customer, or {@code null} if not set
     */
    public AppUser getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the {@link AppUser} who created this customer record.
     *
     * @param createdBy the user responsible for creating this customer
     */
    public void setCreatedBy(AppUser createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Returns the list of {@link Parcel} bookings associated with this customer.
     *
     * @return a list of parcels belonging to this customer, may be empty
     */
    public List<Parcel> getParcels() {
        return parcels;
    }

    /**
     * Sets the list of {@link Parcel} bookings for this customer.
     *
     * @param parcels the list of parcels to associate with this customer
     */
    public void setParcels(List<Parcel> parcels) {
        this.parcels = parcels;
    }

    /**
     * Returns the {@link AppUser} account directly linked to this customer profile.
     *
     * @return the associated {@code AppUser}, or {@code null} if not linked
     */
    public AppUser getAppUser() {
        return appUser;
    }

    /**
     * Sets the {@link AppUser} account directly linked to this customer profile.
     *
     * @param appUser the {@code AppUser} to associate with this customer
     */
    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }
}
