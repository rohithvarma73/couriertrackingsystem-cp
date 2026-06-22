package com.wip.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for capturing new user registration details.
 *
 * <p>This DTO is submitted by a client when registering a new user account in the
 * courier tracking system. It carries all the information needed to create both an
 * {@code AppUser} (authentication credentials and role) and a linked {@code Customer}
 * profile (personal contact details) in a single atomic operation. Bean Validation
 * annotations enforce field-level constraints before the data reaches the service
 * layer. Password confirmation is validated at the service level to ensure both
 * {@code password} and {@code confirmPassword} match.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public class RegisterDto {

    /**
     * Desired login username for the new account. Must not be blank.
     */
    @NotBlank(message = "Username is required")
    private String username;

    /**
     * Plain-text password for the new account. Must not be blank and must be
     * at least 6 characters long. This value is encoded before storage.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /**
     * Confirmation of the password entered in the {@code password} field.
     * The service layer validates that both values are equal before proceeding.
     * Must not be blank.
     */
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    /**
     * Full name of the registering user, used to create the associated customer profile.
     * Must not be blank.
     */
    @NotBlank(message = "Full name is required")
    private String fullName;

    /**
     * Email address for the new account. Used for both the user credentials and
     * the customer profile. Must be a valid email format and must not be blank.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address (e.g. name@example.com)")
    private String email;

    /**
     * 10-digit mobile phone number of the registering user. Stored in the associated
     * customer profile. Must contain exactly 10 numeric digits.
     */
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits (numbers only)")
    private String phone;

    /**
     * Residential or business address of the registering user. Stored in the associated
     * customer profile. Must not be blank.
     */
    @NotBlank(message = "Address is required")
    private String address;

    /**
     * Returns the desired username for the new account.
     *
     * @return the username string
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the desired username for the new account.
     *
     * @param username the username to assign; must not be blank
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the plain-text password supplied during registration.
     *
     * @return the raw password string (not yet encoded)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the plain-text password for the new account.
     *
     * @param password the password to assign; must be at least 6 characters
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the password confirmation string supplied during registration.
     *
     * @return the confirmation password string
     */
    public String getConfirmPassword() {
        return confirmPassword;
    }

    /**
     * Sets the password confirmation string for validation.
     *
     * @param confirmPassword the confirmation password; must match {@code password}
     */
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    /**
     * Returns the full name of the registering user.
     *
     * @return the full name string
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name of the registering user.
     *
     * @param fullName the full name to assign; must not be blank
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Returns the email address supplied during registration.
     *
     * @return the email address string
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address for the new account.
     *
     * @param email the email address to assign; must be a valid format
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the 10-digit phone number supplied during registration.
     *
     * @return the phone number string
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number for the new customer profile.
     *
     * @param phone the phone number to assign; must be exactly 10 digits
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the address supplied during registration.
     *
     * @return the address string
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address for the new customer profile.
     *
     * @param address the address to assign; must not be blank
     */
    public void setAddress(String address) {
        this.address = address;
    }
}
