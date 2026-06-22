package com.wip.controller;

import com.wip.dto.CustomerDto;
import com.wip.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * REST controller for managing customers in the Courier Tracking System.
 *
 * <p>Exposes RESTful endpoints under {@code /api/customers} for full CRUD operations
 * on customer resources. All responses are returned as JSON. Validation is enforced
 * on incoming request bodies via Bean Validation ({@code @Valid}). This controller
 * integrates with the OpenAPI (Swagger) documentation via SpringDoc annotations.</p>
 *
 * @author Dharshan K S (dharshan.ks@wipro.com)
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer Management", description = "APIs for creating, updating, fetching, and deleting customers")
public class CustomerController {

    /** Service layer for all customer business operations. */
    private final CustomerService customerService;

    /**
     * Constructs a {@code CustomerController} with the required {@link CustomerService}.
     *
     * @param customerService the service bean responsible for customer business logic;
     *                        injected automatically by Spring's dependency injection container
     */
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Creates a new customer record in the system.
     *
     * <p>Accepts a validated {@link CustomerDto} request body containing customer details
     * such as name, email, phone number, and address. On success, the persisted customer
     * data (including the generated ID) is returned wrapped in a {@code 200 OK} response.</p>
     *
     * @param customerDto the data transfer object carrying customer details to be persisted;
     *                    must pass all Bean Validation constraints
     * @return a {@link ResponseEntity} containing the saved {@link CustomerDto} with HTTP 200
     */
    @Operation(summary = "Create a new customer", description = "Creates a customer with name, email, phone, and address.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid customer data"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/addCust")
    public ResponseEntity<CustomerDto> addCustomer(@Valid @RequestBody CustomerDto customerDto) {
        return ResponseEntity.ok(customerService.addCustomer(customerDto));
    }

    /**
     * Retrieves a list of all customers registered in the system.
     *
     * <p>Returns every customer record available in the database, mapped to their
     * respective {@link CustomerDto} representations. Returns an empty list when
     * no customers exist.</p>
     *
     * @return a {@link ResponseEntity} containing a {@link List} of {@link CustomerDto} objects
     *         representing all customers, with HTTP 200
     */
    @Operation(summary = "Get all customers", description = "Returns all customers in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customers retrieved successfully")
    })
    @GetMapping("/getAll")
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    /**
     * Retrieves the details of a single customer identified by their unique ID.
     *
     * <p>Looks up the customer with the given {@code id} in the database. If no customer
     * is found with that ID, the service layer throws a {@code ResourceNotFoundException},
     * which results in a {@code 404 Not Found} response.</p>
     *
     * @param id the unique identifier of the customer to retrieve; must be a positive {@link Long}
     * @return a {@link ResponseEntity} containing the matching {@link CustomerDto} with HTTP 200
     * @throws com.wip.exception.ResourceNotFoundException if no customer exists with the given ID (HTTP 404)
     */
    @Operation(summary = "Get customer by ID", description = "Fetches a customer using the customer ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    /**
     * Updates an existing customer record with new data.
     *
     * <p>Applies all non-null fields from the supplied {@link CustomerDto} to the customer
     * identified by {@code id}. The customer must already exist; otherwise a
     * {@code ResourceNotFoundException} is thrown. Bean Validation is enforced on the
     * request body before processing.</p>
     *
     * @param id          the unique identifier of the customer to update
     * @param customerDto the data transfer object containing the updated customer details;
     *                    must pass all Bean Validation constraints
     * @return a {@link ResponseEntity} containing the updated {@link CustomerDto} with HTTP 200
     * @throws com.wip.exception.ResourceNotFoundException if no customer exists with the given ID (HTTP 404)
     */
    @Operation(summary = "Update customer by ID", description = "Updates an existing customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid customer data"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CustomerDto customerDto) {
        return ResponseEntity.ok(customerService.updateCustomer(id, customerDto));
    }

    /**
     * Permanently deletes the customer identified by the given ID.
     *
     * <p>Removes the customer record from the system. If the customer has associated parcel
     * records, the service layer may prevent deletion and throw an appropriate exception.
     * On successful deletion, a {@code 204 No Content} response is returned with no body.</p>
     *
     * @param id the unique identifier of the customer to delete; must be a positive {@link Long}
     * @return a {@link ResponseEntity} with no body and HTTP 204 on success
     * @throws com.wip.exception.ResourceNotFoundException if no customer exists with the given ID (HTTP 404)
     */
    @Operation(summary = "Delete customer by ID", description = "Deletes a customer using the customer ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
