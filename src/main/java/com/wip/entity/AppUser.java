package com.wip.entity;

import jakarta.persistence.*;

/**
 * JPA entity representing a registered application user.
 *
 * <p>Mapped to the {@code users} database table, this entity stores authentication
 * credentials, role information, and a one-to-one relationship with a
 * {@link Customer} profile. Each user is uniquely identified by their
 * {@code username} and {@code email} address. The {@code role} field controls
 * access levels within the system (e.g., {@code ADMIN} or {@code USER}).</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "users")
public class AppUser {

    /**
     * Unique auto-generated surrogate primary key for the user record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    /**
     * Unique login username for the user. Must not be null.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Unique email address associated with the user account. Must not be null.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * The {@link Customer} profile linked to this user account via a one-to-one
     * relationship. Cascade type ALL ensures the customer record is persisted,
     * merged, and removed alongside the user.
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    /**
     * BCrypt-encoded password for the user account. Stored in hashed form; never
     * in plain text. Must not be null.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Authorization role assigned to the user (e.g., {@code "ADMIN"} or
     * {@code "USER"}). Must not be null.
     */
    @Column(nullable = false)
    private String role;

    /**
     * Returns the unique identifier of this user.
     *
     * @return the auto-generated user ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the unique identifier of this user.
     *
     * @param userId the user ID to assign
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Returns the login username of this user.
     *
     * @return the unique username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the login username for this user.
     *
     * @param username the unique username to assign
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the BCrypt-encoded password of this user.
     *
     * @return the encoded password string
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for this user.
     *
     * <p>The value supplied here must already be encoded (e.g., via
     * {@code PasswordEncoder#encode}) before being persisted.</p>
     *
     * @param password the encoded password to store
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the authorization role of this user.
     *
     * @return a role string such as {@code "ADMIN"} or {@code "USER"}
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the authorization role for this user.
     *
     * @param role the role string to assign (e.g., {@code "ADMIN"} or {@code "USER"})
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns the email address associated with this user account.
     *
     * @return the unique email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address for this user account.
     *
     * @param email the unique email address to assign
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the {@link Customer} profile linked to this user.
     *
     * @return the associated {@code Customer} entity, or {@code null} if not set
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Associates a {@link Customer} profile with this user.
     *
     * @param customer the {@code Customer} entity to link to this user
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
