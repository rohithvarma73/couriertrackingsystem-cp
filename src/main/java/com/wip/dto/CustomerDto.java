package com.wip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CustomerDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Customer ID")
    private Long customerId;

    @Schema(example = "Rohith Varma K", description = "Customer name")
    @NotBlank(message = "Customer name is required")
    private String customerName;

    @Schema(example = "rohitvarma@gmail.com", description = "Customer email")
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(example = "9999678901", description = "Customer phone")
    @NotBlank(message = "Phone is required")
    private String phone;

    @Schema(example = "Chennai", description = "Customer address")
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