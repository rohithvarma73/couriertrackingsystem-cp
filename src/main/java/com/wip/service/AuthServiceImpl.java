package com.wip.service;

import com.wip.dto.RegisterDto;
import com.wip.entity.AppUser;
import com.wip.entity.Customer;
import com.wip.repository.AppUserRepository;
import com.wip.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthServiceImpl Component.
 * 
 * Handles operations and data related to AuthServiceImpl.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AppUserRepository appUserRepository, 
                           CustomerRepository customerRepository, 
                           PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void register(RegisterDto registerDto) {
        if (appUserRepository.existsByUsername(registerDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (appUserRepository.existsByEmail(registerDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        Customer customer = new Customer();
        customer.setCustomerName(registerDto.getFullName());
        customer.setEmail(registerDto.getEmail());
        customer.setPhone(registerDto.getPhone());
        customer.setAddress(registerDto.getAddress());

        AppUser user = new AppUser();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRole("USER");
        user.setCustomer(customer);
        
        customer.setCreatedBy(user);
        customer.setAppUser(user);

        appUserRepository.save(user);
    }
}
