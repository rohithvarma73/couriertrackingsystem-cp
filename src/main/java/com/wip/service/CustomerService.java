package com.wip.service;

import com.wip.dto.CustomerDto;
import java.util.List;

/**
 * CustomerService Component.
 * 
 * Handles operations and data related to CustomerService.
 */
public interface CustomerService {
    CustomerDto addCustomer(CustomerDto customerDto);
    List<CustomerDto> getAllCustomers();
    CustomerDto getCustomerById(Long id);
    CustomerDto updateCustomer(Long id, CustomerDto customerDto);
    void deleteCustomer(Long id);
    List<CustomerDto> search(String keyword);
}
