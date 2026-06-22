package com.wip.service;

import com.wip.dto.CustomerDto;
import com.wip.entity.AppUser;
import com.wip.entity.Customer;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.AppUserRepository;
import com.wip.repository.CustomerRepository;
import com.wip.security.CurrentUserUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for managing customer records with role-based access control.
 *
 * <p>This class implements {@link CustomerService} and provides the core business logic
 * for all customer-related CRUD operations. Access control is enforced throughout:
 * administrators ({@code ADMIN} role) have unrestricted access to all customer data,
 * while regular users ({@code USER} role) are restricted to viewing and modifying only
 * the customer records they themselves have created. Deletion is an admin-only operation
 * and is further guarded against removing customers who have associated parcel bookings.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    /**
     * Repository for persisting and querying {@link Customer} records.
     */
    private final CustomerRepository customerRepository;

    /**
     * Repository for querying {@link AppUser} records to resolve the currently
     * authenticated user.
     */
    private final AppUserRepository appUserRepository;

    /**
     * Constructs a {@code CustomerServiceImpl} with the required repository dependencies.
     *
     * @param customerRepository the repository for {@link Customer} persistence operations
     * @param appUserRepository  the repository for {@link AppUser} lookup operations
     */
    public CustomerServiceImpl(CustomerRepository customerRepository,
                               AppUserRepository appUserRepository) {
        this.customerRepository = customerRepository;
        this.appUserRepository = appUserRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolves the currently authenticated user from the security context, builds
     * a new {@link Customer} entity from the supplied DTO, assigns the current user as
     * the creator, and persists the record.</p>
     *
     * @param customerDto the data transfer object containing the new customer's details
     * @return the persisted customer mapped to a {@link CustomerDto}
     * @throws ResourceNotFoundException if the currently authenticated user is not found
     */
    @Override
    public CustomerDto addCustomer(CustomerDto customerDto) {
        String username = CurrentUserUtil.getCurrentUsername();
        AppUser currentUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Customer customer = new Customer();
        customer.setCustomerName(customerDto.getCustomerName());
        customer.setEmail(customerDto.getEmail());
        customer.setPhone(customerDto.getPhone());
        customer.setAddress(customerDto.getAddress());
        customer.setCreatedBy(currentUser);

        return toDto(customerRepository.save(customer));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Administrators receive all customer records from the repository. Non-admin
     * users receive only the subset of customers created under their username.</p>
     *
     * @return a list of accessible {@link CustomerDto} objects
     */
    @Override
    public List<CustomerDto> getAllCustomers() {
        if (CurrentUserUtil.isAdmin()) {
            return customerRepository.findAll().stream().map(this::toDto).toList();
        }
        String username = CurrentUserUtil.getCurrentUsername();
        return customerRepository.findByCreatedBy_Username(username)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>After finding the customer by ID, non-admin users are subject to an ownership
     * check: if the customer's {@code createdBy} user does not match the authenticated
     * user, the request is treated as a not-found to prevent information disclosure.</p>
     *
     * @param id the unique ID of the customer to retrieve
     * @return the matching {@link CustomerDto}
     * @throws ResourceNotFoundException if the customer does not exist or is not
     *         accessible to the current user
     */
    @Override
    public CustomerDto getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!CurrentUserUtil.isAdmin()) {
            String username = CurrentUserUtil.getCurrentUsername();
            if (customer.getCreatedBy() == null || !username.equals(customer.getCreatedBy().getUsername())) {
                throw new ResourceNotFoundException("Customer not found");
            }
        }

        return toDto(customer);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Loads the existing customer record, enforces ownership for non-admin users,
     * applies field updates from the supplied DTO, and persists the changes.</p>
     *
     * @param id          the unique ID of the customer to update
     * @param customerDto the DTO containing the updated field values
     * @return the updated {@link CustomerDto}
     * @throws ResourceNotFoundException if the customer does not exist or is not
     *         accessible to the current user
     */
    @Override
    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!CurrentUserUtil.isAdmin()) {
            String username = CurrentUserUtil.getCurrentUsername();
            if (customer.getCreatedBy() == null || !username.equals(customer.getCreatedBy().getUsername())) {
                throw new ResourceNotFoundException("Customer not found");
            }
        }

        customer.setCustomerName(customerDto.getCustomerName());
        customer.setEmail(customerDto.getEmail());
        customer.setPhone(customerDto.getPhone());
        customer.setAddress(customerDto.getAddress());

        return toDto(customerRepository.save(customer));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Deletion is restricted exclusively to administrators. Before deleting, the
     * service checks whether any parcel records are associated with the customer;
     * if parcels exist, deletion is rejected to preserve referential integrity.</p>
     *
     * @param id the unique ID of the customer to delete
     * @throws IllegalStateException     if the current user is not an administrator, or if
     *                                   the customer has associated parcel bookings
     * @throws ResourceNotFoundException if no customer with the given ID exists
     */
    @Override
    public void deleteCustomer(Long id) {
        if (!CurrentUserUtil.isAdmin()) {
            throw new IllegalStateException("Only administrators can delete customers");
        }

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (customerRepository.existsParcelsByCustomerId(id)) {
            throw new IllegalStateException("Cannot delete customer because parcels exist for this customer");
        }

        customerRepository.delete(customer);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #getAllCustomers()} to obtain the role-scoped customer list,
     * then filters the results in-memory by matching the keyword (case-insensitively)
     * against the customer ID, name, phone, email, and address fields.</p>
     *
     * @param keyword the search term; {@code null} or blank returns all accessible customers
     * @return a filtered list of matching {@link CustomerDto} objects
     */
    @Override
    public List<CustomerDto> search(String keyword) {
        List<CustomerDto> customers = getAllCustomers();
        if (keyword == null || keyword.isBlank()) {
            return customers;
        }

        String k = keyword.toLowerCase();
        return customers.stream()
                .filter(c ->
                        (c.getCustomerId() != null && String.valueOf(c.getCustomerId()).contains(k)) ||
                        (c.getCustomerName() != null && c.getCustomerName().toLowerCase().contains(k)) ||
                        (c.getPhone() != null && c.getPhone().toLowerCase().contains(k)) ||
                        (c.getEmail() != null && c.getEmail().toLowerCase().contains(k)) ||
                        (c.getAddress() != null && c.getAddress().toLowerCase().contains(k)))
                .toList();
    }

    /**
     * Converts a {@link Customer} entity to a {@link CustomerDto}.
     *
     * <p>Maps only the flat fields of the customer (ID, name, email, phone, address)
     * into the DTO, omitting relational fields not needed in the API response.</p>
     *
     * @param customer the {@link Customer} entity to convert; must not be {@code null}
     * @return a populated {@link CustomerDto} representing the given customer
     */
    private CustomerDto toDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(customer.getCustomerId());
        dto.setCustomerName(customer.getCustomerName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        return dto;
    }
}
