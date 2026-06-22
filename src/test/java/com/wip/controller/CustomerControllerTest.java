package com.wip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.dto.CustomerDto;
import com.wip.exception.ResourceNotFoundException;
import com.wip.security.CustomUserDetailsService;
import com.wip.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link CustomerController} covering all REST endpoints.
 *
 * <p>Uses {@link WebMvcTest} to load only the web layer slice, with
 * {@link CustomerService} mocked via Mockito. Security is exercised using
 * {@link WithMockUser} to simulate authenticated and unauthenticated callers.
 * Scenarios validated include successful retrieval, creation, update, and
 * deletion of customers, as well as Bean Validation constraint failures and
 * authorisation guards.</p>
 *
 * @author Dharshan K S
 * @version 1.0
 * @since 1.0
 */
@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    /** MockMvc instance used to perform HTTP requests against the controller. */
    @Autowired
    private MockMvc mockMvc;

    /** Mocked {@link CustomerService} dependency injected into the controller under test. */
    @MockitoBean
    private CustomerService customerService;

    // Required by SecurityConfig — not directly used in these tests
    /** Mocked {@link CustomUserDetailsService} required by the Spring Security configuration. */
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    /** Jackson {@link ObjectMapper} used to serialise request bodies to JSON. */
    @Autowired
    private ObjectMapper objectMapper;

    // ── GET /api/customers/getAll ─────────────────────────────────────────────

    /**
     * Verifies that {@code GET /api/customers/getAll} returns HTTP 200 and a non-empty
     * JSON array when at least one customer exists and the caller has the {@code ADMIN} role.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_asAdmin_returns200WithList() throws Exception {
        CustomerDto dto = makeDto(1L, "Rahul Sharma", "rahul@example.com", "9876543210", "Bangalore");
        when(customerService.getAllCustomers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/customers/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].customerName").value("Rahul Sharma"));
    }

    /**
     * Verifies that {@code GET /api/customers/getAll} returns HTTP 200 with an empty
     * JSON array when no customers exist in the system.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_emptyList_returns200WithEmptyArray() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of());

        mockMvc.perform(get("/api/customers/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /api/customers/{id} ───────────────────────────────────────────────

    /**
     * Verifies that {@code GET /api/customers/{id}} returns HTTP 200 with the correct
     * customer payload when the requested customer ID exists.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_existingId_returns200() throws Exception {
        CustomerDto dto = makeDto(5L, "Priya Mehta", "priya@example.com", "9123456780", "Mumbai");
        when(customerService.getCustomerById(5L)).thenReturn(dto);

        mockMvc.perform(get("/api/customers/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(5))
                .andExpect(jsonPath("$.customerName").value("Priya Mehta"));
    }

    /**
     * Verifies that {@code GET /api/customers/{id}} returns HTTP 404 when the requested
     * customer ID does not exist, producing a {@link ResourceNotFoundException}.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_nonExistingId_returns404() throws Exception {
        when(customerService.getCustomerById(999L))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/customers/addCust ───────────────────────────────────────────

    /**
     * Verifies that {@code POST /api/customers/addCust} returns HTTP 200 with the
     * persisted customer (including a generated ID) when all required fields are valid.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void addCustomer_validPayload_returns200() throws Exception {
        CustomerDto request = makeDto(null, "New Customer", "new@example.com", "9871234560", "Delhi");
        CustomerDto response = makeDto(10L, "New Customer", "new@example.com", "9871234560", "Delhi");

        when(customerService.addCustomer(any(CustomerDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/customers/addCust")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(10))
                .andExpect(jsonPath("$.customerName").value("New Customer"));
    }

    /**
     * Verifies that {@code POST /api/customers/addCust} returns HTTP 400 when the
     * supplied email address does not conform to a valid email format, triggering
     * Bean Validation constraints.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void addCustomer_invalidEmail_returns400() throws Exception {
        CustomerDto request = makeDto(null, "Bad Email", "not-an-email", "9871234560", "Delhi");

        mockMvc.perform(post("/api/customers/addCust")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that {@code POST /api/customers/addCust} returns HTTP 400 when the
     * phone number does not meet the required ten-digit format constraint.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void addCustomer_phoneNotTenDigits_returns400() throws Exception {
        CustomerDto request = makeDto(null, "Bad Phone", "ok@example.com", "123", "Delhi");

        mockMvc.perform(post("/api/customers/addCust")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that {@code POST /api/customers/addCust} returns HTTP 400 when the
     * customer name is blank, violating the {@code @NotBlank} constraint.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void addCustomer_missingName_returns400() throws Exception {
        CustomerDto request = makeDto(null, "", "ok@example.com", "9871234560", "Delhi");

        mockMvc.perform(post("/api/customers/addCust")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/customers/{id} ───────────────────────────────────────────────

    /**
     * Verifies that {@code PUT /api/customers/{id}} returns HTTP 200 with the updated
     * customer details when a valid payload is provided for an existing customer.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCustomer_validPayload_returns200() throws Exception {
        CustomerDto request = makeDto(5L, "Updated Name", "updated@example.com", "9000000001", "Pune");
        when(customerService.updateCustomer(anyLong(), any(CustomerDto.class))).thenReturn(request);

        mockMvc.perform(put("/api/customers/5")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Updated Name"));
    }

    // ── DELETE /api/customers/{id} ────────────────────────────────────────────

    /**
     * Verifies that {@code DELETE /api/customers/{id}} returns HTTP 204 No Content
     * when the customer exists and has no associated parcels.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCustomer_existingId_returns204() throws Exception {
        doNothing().when(customerService).deleteCustomer(5L);

        mockMvc.perform(delete("/api/customers/5").with(csrf()))
                .andExpect(status().isNoContent());
    }

    /**
     * Verifies that {@code DELETE /api/customers/{id}} returns HTTP 400 Bad Request
     * when the customer still has associated parcels, preventing deletion per business rules.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCustomer_hasParcels_returns400() throws Exception {
        doThrow(new IllegalStateException("Cannot delete customer because parcels exist"))
                .when(customerService).deleteCustomer(5L);

        mockMvc.perform(delete("/api/customers/5").with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ── auth guard ────────────────────────────────────────────────────────────

    /**
     * Verifies that {@code GET /api/customers/getAll} returns HTTP 401 Unauthorized
     * when the request is made without any authentication credentials, confirming
     * that the security configuration protects all customer endpoints.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    void getAll_unauthenticated_returns401or302() throws Exception {
        mockMvc.perform(get("/api/customers/getAll"))
                .andExpect(status().isUnauthorized()); // API endpoints return 401
    }

    // ── helper ────────────────────────────────────────────────────────────────

    /**
     * Constructs a {@link CustomerDto} with the supplied field values for use in tests.
     *
     * @param id      the customer ID (may be {@code null} for creation scenarios)
     * @param name    the customer's full name
     * @param email   the customer's email address
     * @param phone   the customer's ten-digit phone number
     * @param address the customer's physical address
     * @return a fully populated {@link CustomerDto} instance
     */
    private CustomerDto makeDto(Long id, String name, String email, String phone, String address) {
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(id);
        dto.setCustomerName(name);
        dto.setEmail(email);
        dto.setPhone(phone);
        dto.setAddress(address);
        return dto;
    }
}