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

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private AppUser adminUser;
    private AppUser regularUser;
    private Customer customer;

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

    @Test
    void getCustomerById_notFound_throwsException() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(customerRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(99L));
        }
    }

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

    @Test
    void deleteCustomer_asNonAdmin_throwsIllegalState() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(false);

            assertThrows(IllegalStateException.class, () -> customerService.deleteCustomer(10L));
            verify(customerRepository, never()).delete(any());
        }
    }

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

    @Test
    void search_byName_returnsMatchingCustomer() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(customerRepository.findAll()).thenReturn(List.of(customer));

            List<CustomerDto> result = customerService.search("Test");

            assertEquals(1, result.size());
        }
    }

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