package com.wip.service;

import com.wip.dto.CustomerDto;
import com.wip.entity.Customer;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void testAddCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId(1L);
        customer.setCustomerName("Rahul Sharma");
        customer.setEmail("rahul@gmail.com");
        customer.setPhone("9876543210");
        customer.setAddress("Bangalore");

        when(customerRepository.save(org.mockito.ArgumentMatchers.any(Customer.class))).thenReturn(customer);

        CustomerDto dto = new CustomerDto();
        dto.setCustomerName("Rahul Sharma");
        dto.setEmail("rahul@gmail.com");
        dto.setPhone("9876543210");
        dto.setAddress("Bangalore");

        CustomerDto result = customerService.addCustomer(dto);

        assertEquals(1L, result.getCustomerId());
        assertEquals("Rahul Sharma", result.getCustomerName());
    }

    @Test
    void testGetCustomerById() {
        Customer customer = new Customer();
        customer.setCustomerId(1L);
        customer.setCustomerName("Rahul Sharma");
        customer.setEmail("rahul@gmail.com");
        customer.setPhone("9876543210");
        customer.setAddress("Bangalore");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerDto result = customerService.getCustomerById(1L);

        assertEquals(1L, result.getCustomerId());
        assertEquals("Rahul Sharma", result.getCustomerName());
    }

    @Test
    void testGetCustomerByIdNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(1L));
    }

    @Test
    void testGetAllCustomers() {
        Customer customer = new Customer();
        customer.setCustomerId(1L);
        customer.setCustomerName("Rahul Sharma");
        customer.setEmail("rahul@gmail.com");
        customer.setPhone("9876543210");
        customer.setAddress("Bangalore");

        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<CustomerDto> result = customerService.getAllCustomers();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCustomerId());
    }
}