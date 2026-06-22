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
 * Service implementation that handles user registration for the courier tracking system.
 *
 * <p>This class implements {@link AuthService} and orchestrates the complete user
 * self-registration workflow. It validates uniqueness constraints on username and email,
 * confirms that the supplied passwords match, encodes the plain-text password using
 * a {@link PasswordEncoder}, and then atomically creates both an {@link AppUser}
 * (with the {@code USER} role) and a linked {@link Customer} profile in a single
 * database transaction. The newly registered user is automatically assigned the
 * {@code USER} role and cannot self-assign the {@code ADMIN} role.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Service
public class AuthServiceImpl implements AuthService {

    /**
     * Repository for persisting and querying {@link AppUser} records.
     */
    private final AppUserRepository appUserRepository;

    /**
     * Repository for persisting {@link Customer} records.
     */
    private final CustomerRepository customerRepository;

    /**
     * Spring Security password encoder used to hash plain-text passwords
     * before storing them in the database.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs an {@code AuthServiceImpl} with the required dependencies.
     *
     * @param appUserRepository  the repository for {@link AppUser} persistence operations
     * @param customerRepository the repository for {@link Customer} persistence operations
     * @param passwordEncoder    the encoder used to hash user passwords before storage
     */
    public AuthServiceImpl(AppUserRepository appUserRepository,
                           CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The registration process performs the following steps in order:</p>
     * <ol>
     *   <li>Checks that the requested username is not already taken.</li>
     *   <li>Checks that the requested email address is not already registered.</li>
     *   <li>Verifies that {@code password} and {@code confirmPassword} are identical.</li>
     *   <li>Builds a {@link Customer} entity from the registration data.</li>
     *   <li>Builds an {@link AppUser} with the encoded password and role {@code "USER"},
     *       linking it to the new customer.</li>
     *   <li>Sets the bidirectional relationship between the user and the customer.</li>
     *   <li>Persists the user (and cascades the customer save) in a single transaction.</li>
     * </ol>
     *
     * @param registerDto the registration payload; must not be {@code null}
     * @throws RuntimeException if the username already exists, the email is already
     *                          registered, or the passwords do not match
     */
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
