package com.wip.service;

import com.wip.dto.CustomerDto;
import java.util.List;

/**
 * Service interface defining the business operations for managing customer records.
 *
 * <p>This interface provides the contract for creating, retrieving, updating, deleting,
 * and searching customer profiles within the courier tracking system. Implementations
 * must enforce role-based access control so that non-admin users can only access and
 * modify their own customer records, while administrators have unrestricted access to
 * all customer data.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public interface CustomerService {

    /**
     * Creates and persists a new customer record using the details from the provided DTO.
     *
     * <p>The currently authenticated user is automatically set as the creator of the
     * new customer record.</p>
     *
     * @param customerDto the data transfer object containing the new customer's details;
     *                    must not be {@code null}
     * @return a {@link CustomerDto} representing the newly created customer, including
     *         the server-assigned {@code customerId}
     */
    CustomerDto addCustomer(CustomerDto customerDto);

    /**
     * Retrieves all customer records visible to the currently authenticated user.
     *
     * <p>Administrators receive all customer records. Non-admin users receive only the
     * customers they have created.</p>
     *
     * @return a list of {@link CustomerDto} objects; may be empty if no customers exist
     *         or are accessible to the current user
     */
    List<CustomerDto> getAllCustomers();

    /**
     * Retrieves a single customer record by its unique identifier.
     *
     * <p>Non-admin users may only retrieve customers that they created; attempts to
     * access another user's customer record will result in a not-found exception.</p>
     *
     * @param id the unique ID of the customer to retrieve
     * @return the matching {@link CustomerDto}
     * @throws com.wip.exception.ResourceNotFoundException if no customer with the given ID
     *         exists or if the current user does not have access to it
     */
    CustomerDto getCustomerById(Long id);

    /**
     * Updates an existing customer record with the details provided in the DTO.
     *
     * <p>Non-admin users may only update customers that they created. The customer's
     * name, email, phone, and address fields are updated.</p>
     *
     * @param id          the unique ID of the customer to update
     * @param customerDto the data transfer object containing the updated customer details
     * @return the updated {@link CustomerDto}
     * @throws com.wip.exception.ResourceNotFoundException if no customer with the given ID
     *         exists or if the current user does not have access to it
     */
    CustomerDto updateCustomer(Long id, CustomerDto customerDto);

    /**
     * Deletes a customer record by its unique identifier.
     *
     * <p>This operation is restricted to administrators. Customers with existing parcel
     * bookings cannot be deleted to preserve referential integrity.</p>
     *
     * @param id the unique ID of the customer to delete
     * @throws IllegalStateException if the current user is not an administrator, or if
     *         the customer has one or more associated parcel bookings
     * @throws com.wip.exception.ResourceNotFoundException if no customer with the given ID exists
     */
    void deleteCustomer(Long id);

    /**
     * Searches for customers matching the given keyword across multiple fields.
     *
     * <p>The search is performed against the customer ID, name, phone, email, and address
     * fields in a case-insensitive manner. The result set is scoped to records accessible
     * to the currently authenticated user (all records for admins; own records for users).
     * If the keyword is {@code null} or blank, all accessible customers are returned.</p>
     *
     * @param keyword the search term to filter customers by; {@code null} or blank returns all
     * @return a list of matching {@link CustomerDto} objects; may be empty if no matches found
     */
    List<CustomerDto> search(String keyword);
}
