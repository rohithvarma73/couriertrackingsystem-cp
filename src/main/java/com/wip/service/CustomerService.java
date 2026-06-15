package com.wip.service;

import com.wip.dto.CustomerDto;
import java.util.List;

public interface CustomerService {
    CustomerDto addCustomer(CustomerDto customerDto);
    List<CustomerDto> getAllCustomers();
    CustomerDto getCustomerById(Long id);
    CustomerDto updateCustomer(Long id, CustomerDto customerDto);
    void deleteCustomer(Long id);
}