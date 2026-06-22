package com.wip.service;

import com.wip.dto.CustomerDto;
import com.wip.entity.AppUser;
import com.wip.entity.Customer;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.AppUserRepository;
import com.wip.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wip.security.CurrentUserUtil;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CustomerServiceImpl} covering all service-layer operations.
 *
 * <p>Uses the Mockito JUnit 5 extension to isolate the service from its repository
 * dependencies. {@link CurrentUserUtil} is mocked as a static utility to simulate
 * different caller identities ({@code ADMIN} vs. regular {@code USER}) without
 * requiring a full Spring Security context. Scenarios validated include:</p>
 * <ul>
 *   <li>Adding a customer as admin and handling unknown caller identities.</li>
 *   <li>Listing all customers as admin versus showing only owned customers to regular users.</li>
 *   <li>Retrieving a customer by ID with access-control enforcement.</li>
 *   <li>Updating customer details as admin.</li>
 *   <li>Deleting customers with parcel-existence guard and role guard.</li>
 *   <li>Searching customers by name with matching and non-matching queries.</li>
 * </ul>
 *
 * @author Dharshan K S
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    /** Mocked repository for {@link Customer} persistence operations. */
    @Mock
    private CustomerRepository customerRepository;

    /** Mocked repository for {@link AppUser} lookup operations. */
    @Mock
    private AppUserRepository appUserRepository;

    /** The {@link CustomerServiceImpl} instance under test with mocked dependencies injected. */
    @InjectMocks
    private CustomerServiceImpl customerService;

    /** Simulated admin user used in test fixtures. */
    private AppUser adminUser;

    /** Simulated regular (non-admin) user used in test fixtures. */
    private AppUser regularUser;

    /** Sample {@link Customer} entity used across multiple test cases. */
    private Customer customer;

    /**
     * Initialises shared test fixtures — admin user, regular user, and a sample customer —
     * before each test method executes.
     */
    @BeforeEach
    void setUp() {
        adminUser = new AppUser();
        adminUser.setUserId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole("ADMIN");

        regularUser = new AppUser();
        regularUser.setUserId(2L);
        regularUser.setUsername("testuser");
        regularUser.setRole("USER");

        customer = new Customer();
        customer.setCustomerId(10L);
        customer.setCustomerName("Test Customer");
        customer.setEmail("test@example.com");
        customer.setPhone("9876543210");
        customer.setAddress("Mumbai");
        customer.setCreatedBy(adminUser);
    }

    // ── addCustomer ──────────────────────────────────────────────────────────

    /**
     * Verifies that {@link CustomerServiceImpl#addCustomer(CustomerDto)} successfully
     * persists a new customer and returns the populated DTO when the caller is an admin
     * with a valid user account in the repository.
     */
    @Test
    void addCustomer_asAdmin_savesSuccessfully() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("admin");
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);

            when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            CustomerDto dto = new CustomerDto();
            dto.setCustomerName("Test Customer");
            dto.setEmail("test@example.com");
            dto.setPhone("9876543210");
            dto.setAddress("Mumbai");

            CustomerDto result = customerService.addCustomer(dto);

            assertNotNull(result);
            assertEquals("Test Customer", result.getCustomerName());
            assertEquals("9876543210", result.getPhone());
            verify(customerRepository).save(any(Customer.class));
        }
    }

    /**
     * Verifies that {@link CustomerServiceImpl#addCustomer(CustomerDto)} throws a
     * {@link ResourceNotFoundException} and never calls {@code save} when the
     * authenticated username does not correspond to any user account in the repository.
     */
    @Test
    void addCustomer_userNotFound_throwsException() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("ghost");

            when(appUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            CustomerDto dto = new CustomerDto();
            dto.setCustomerName("Ghost User");
            dto.setEmail("ghost@example.com");
            dto.setPhone("9876543210");
            dto.setAddress("Nowhere");

            assertThrows(ResourceNotFoundException.class, () -> customerService.addCustomer(dto));
            verify(customerRepository, never()).save(any());
        }
    }

    // ── getAllCustomers ───────────────────────────────────────────────────────

    /**
     * Verifies that {@link CustomerServiceImpl#getAllCustomers()} returns all customers
     * in the system without filtering when the caller has the {@code ADMIN} role.
     */
    @Test
    void getAllCustomers_asAdmin_returnsAll() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(customerRepository.findAll()).thenReturn(List.of(customer));

            List<CustomerDto> result = customerService.getAllCustomers();

            assertEquals(1, result.size());
            assertEquals("Test Customer", result.get(0).getCustomerName());
        }
    }

    /**
     * Verifies that {@link CustomerServiceImpl#getAllCustomers()} returns only the
     * customers created by the authenticated user when the caller has the regular
     * {@code USER} role, enforcing data isolation between users.
     */
    @Test
    void getAllCustomers_asUser_returnsOnlyOwnCustomers() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(false);
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("testuser");

            customer.setCreatedBy(regularUser);
            when(customerRepository.findByCreatedBy_Username("testuser")).thenReturn(List.of(customer));

            List<CustomerDto> result = customerService.getAllCustomers();

            assertEquals(1, result.size());
            assertEquals("Test Customer", result.get(0).getCustomerName());
        }
    }

    /**
     * Verifies that {@link CustomerServiceImpl#getAllCustomers()} returns an empty list
     * rather than throwing an exception when no customers exist in the database.
     */
    @Test
    void getAllCustomers_emptyDatabase_returnsEmptyList() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(customerRepository.findAll()).thenReturn(List.of());

            List<CustomerDto> result = customerService.getAllCustomers();

            assertTrue(result.isEmpty());
        }
    }

    // ── getCustomerById ───────────────────────────────────────────────────────

    /**
     * Verifies that {@link CustomerServiceImpl#getCustomerById(Long)} returns the
     * correct customer DTO when the caller is an admin and the customer ID exists.
     */
    @Test
    void getCustomerById_asAdmin_returnsCustomer() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));

            CustomerDto result = customerService.getCustomerById(10L);

            assertEquals(10L, result.getCustomerId());
            assertEquals("Test Customer", result.getCustomerName());
        }
    }

    /**
     * Verifies that {@link CustomerServiceImpl#getCustomerById(Long)} throws a
     * {@link ResourceNotFoundException} when the requested customer ID does not exist,
     * even when the caller is an admin.
     */
    @Test
    void getCustomerById_notFound_throwsException() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(customerRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(99L));
        }
    }

    /**
     * Verifies that {@link CustomerServiceImpl#getCustomerById(Long)} throws a
     * {@link ResourceNotFoundException} when a regular user attempts to retrieve a
     * customer that was created by a different user, enforcing ownership-based access control.
     */
    @Test
    void getCustomerById_asUser_accessOtherUserCustomer_throwsException() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(false);
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("otheruser");

            // customer.createdBy = adminUser (not "otheruser")
            when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));

            assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(10L));
        }
    }

    // ── updateCustomer ────────────────────────────────────────────────────────

    /**
     * Verifies that {@link CustomerServiceImpl#updateCustomer(Long, CustomerDto)} applies
     * the supplied changes to the existing customer entity and returns the updated DTO
     * when the caller is an admin.
     */
    @Test
    void updateCustomer_asAdmin_updatesSuccessfully() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);

            Customer updated = new Customer();
            updated.setCustomerId(10L);
            updated.setCustomerName("Updated Name");
            updated.setEmail("updated@example.com");
            updated.setPhone("9999999999");
            updated.setAddress("Delhi");
            updated.setCreatedBy(adminUser);

            when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
            when(customerRepository.save(any(Customer.class))).thenReturn(updated);

            CustomerDto dto = new CustomerDto();
            dto.setCustomerName("Updated Name");
            dto.setEmail("updated@example.com");
            dto.setPhone("9999999999");
            dto.setAddress("Delhi");

            CustomerDto result = customerService.updateCustomer(10L, dto);

            assertEquals("Updated Name", result.getCustomerName());
            verify(customerRepository).save(any(Customer.class));
        }
    }

    // ── deleteCustomer ────────────────────────────────────────────────────────

    /**
     * Verifies that {@link CustomerServiceImpl#deleteCustomer(Long)} throws an
     * {@link IllegalStateException} and never calls {@code delete} when the caller
     * does not have the {@code ADMIN} role, enforcing delete privilege restrictions.
     */
    @Test
    void deleteCustomer_asNonAdmin_throwsIllegalState() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(false);

            assertThrows(IllegalStateException.class, () -> customerService.deleteCustomer(10L));
            verify(customerRepository, never()).delete(any());
        }
    }

    /**
     * Verifies that {@link CustomerServiceImpl#deleteCustomer(Long)} throws an
     * {@link IllegalStateException} and never calls {@code delete} when the customer
     * still has associated parcels, protecting referential integrity.
     */
    @Test
    void deleteCustomer_withParcels_throwsIllegalState() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);

            when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
            when(customerRepository.existsParcelsByCustomerId(10L)).thenReturn(true);

            assertThrows(IllegalStateException.class, () -> customerService.deleteCustomer(10L));
            verify(customerRepository, never()).delete(any());
        }
    }

    /**
     * Verifies that {@link CustomerServiceImpl#deleteCustomer(Long)} successfully removes
     * the customer without throwing an exception when the caller is an admin and the
     * customer has no associated parcels.
     */
    @Test
    void deleteCustomer_noParcels_deletesSuccessfully() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);

            when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
            when(customerRepository.existsParcelsByCustomerId(10L)).thenReturn(false);
            doNothing().when(customerRepository).delete(customer);

            assertDoesNotThrow(() -> customerService.deleteCustomer(10L));
            verify(customerRepository).delete(customer);
        }
    }

    // ── search ────────────────────────────────────────────────────────────────

    /**
     * Verifies that {@link CustomerServiceImpl#search(String)} returns a list containing
     * only the customers whose names match the given search keyword (case-insensitive prefix
     * or substring match).
     */
    @Test
    void search_byName_returnsMatchingCustomer() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(customerRepository.findAll()).thenReturn(List.of(customer));

            List<CustomerDto> result = customerService.search("Test");

            assertEquals(1, result.size());
        }
    }

    /**
     * Verifies that {@link CustomerServiceImpl#search(String)} returns an empty list
     * when no customer names match the supplied search keyword.
     */
    @Test
    void search_noMatch_returnsEmpty() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(customerRepository.findAll()).thenReturn(List.of(customer));

            List<CustomerDto> result = customerService.search("zzznomatch");

            assertTrue(result.isEmpty());
        }
    }
}