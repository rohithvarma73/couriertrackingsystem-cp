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

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AppUserRepository appUserRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               AppUserRepository appUserRepository) {
        this.customerRepository = customerRepository;
        this.appUserRepository = appUserRepository;
    }

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

    @Override
    public List<CustomerDto> getAllCustomers() {
        String username = CurrentUserUtil.getCurrentUsername();
        return customerRepository.findByCreatedBy_Username(username)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public CustomerDto getCustomerById(Long id) {
        String username = CurrentUserUtil.getCurrentUsername();
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (customer.getCreatedBy() == null || !username.equals(customer.getCreatedBy().getUsername())) {
            throw new ResourceNotFoundException("Customer not found");
        }

        return toDto(customer);
    }

    @Override
    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        String username = CurrentUserUtil.getCurrentUsername();
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (customer.getCreatedBy() == null || !username.equals(customer.getCreatedBy().getUsername())) {
            throw new ResourceNotFoundException("Customer not found");
        }

        customer.setCustomerName(customerDto.getCustomerName());
        customer.setEmail(customerDto.getEmail());
        customer.setPhone(customerDto.getPhone());
        customer.setAddress(customerDto.getAddress());

        return toDto(customerRepository.save(customer));
    }

    @Override
    public void deleteCustomer(Long id) {
        String username = CurrentUserUtil.getCurrentUsername();
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (customer.getCreatedBy() == null || !username.equals(customer.getCreatedBy().getUsername())) {
            throw new ResourceNotFoundException("Customer not found");
        }

        if (customerRepository.existsParcelsByCustomerId(id)) {
            throw new IllegalStateException("Cannot delete customer because parcels exist for this customer");
        }

        customerRepository.delete(customer);
    }

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