package com.wip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * CustomerDto Component.
 * 
 * Handles operations and data related to CustomerDto.
 */
public class CustomerDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Customer ID")
    private Long customerId;

    @Schema(example = "Rohith Varma K", description = "Customer name")
    @NotBlank(message = "Customer name is required")
    private String customerName;

    @Schema(example = "rohith@example.com", description = "Customer email")
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address (e.g. name@example.com)")
    private String email;

    @Schema(example = "9876543210", description = "10-digit mobile number")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits (numbers only)")
    private String phone;

    @Schema(example = "Chennai, Tamil Nadu", description = "Customer address")
    @NotBlank(message = "Address is required")
    private String address;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
