package com.wip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for transferring customer information between the API layer and the service layer.
 *
 * <p>This DTO is used for both creating and updating customer records. It carries
 * validated customer details — name, email, phone, and address — between the REST
 * controller and the business logic layer, decoupling the API contract from the
 * underlying JPA entity. Bean Validation annotations ensure that all required fields
 * conform to expected formats before they reach the service layer. The
 * {@code customerId} field is read-only and populated only in response payloads.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public class CustomerDto {

    /**
     * The unique identifier of the customer. Populated by the server in responses;
     * should not be supplied by the client on create or update requests.
     */
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Customer ID")
    private Long customerId;

    /**
     * Full name of the customer. Must not be blank.
     */
    @Schema(example = "Rohith Varma K", description = "Customer name")
    @NotBlank(message = "Customer name is required")
    private String customerName;

    /**
     * Email address of the customer. Must be a valid email format and must not be blank.
     */
    @Schema(example = "rohith@example.com", description = "Customer email")
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address (e.g. name@example.com)")
    private String email;

    /**
     * 10-digit mobile phone number of the customer. Must contain exactly 10 numeric digits.
     */
    @Schema(example = "9876543210", description = "10-digit mobile number")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits (numbers only)")
    private String phone;

    /**
     * Physical address of the customer. Must not be blank.
     */
    @Schema(example = "Chennai, Tamil Nadu", description = "Customer address")
    @NotBlank(message = "Address is required")
    private String address;

    /**
     * Returns the unique identifier of this customer (read-only in API requests).
     *
     * @return the customer ID, or {@code null} if not yet persisted
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
     * @param customerName the full name to assign; must not be blank
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
     * @param email the email address to assign; must be a valid format
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the phone number of this customer.
     *
     * @return a 10-digit numeric phone number string
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number of this customer.
     *
     * @param phone the phone number to assign; must be exactly 10 digits
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
     * @param address the address to assign; must not be blank
     */
    public void setAddress(String address) {
        this.address = address;
    }
}
