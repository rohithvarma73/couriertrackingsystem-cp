package com.wip.security;

import com.wip.entity.AppUser;
import com.wip.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService} for the Courier Tracking System.
 *
 * <p>This service is responsible for retrieving user authentication and authorization details
 * during the Spring Security login process. It queries the {@link AppUserRepository} to locate
 * an {@link AppUser} by their username, and wraps the result in a {@link CustomUserDetails}
 * object that exposes the user's credentials and granted authorities to the security framework.</p>
 *
 * <p>This bean is consumed by the {@code DaoAuthenticationProvider} configured in
 * {@code SecurityConfig} and is automatically discovered by Spring's component scan
 * via the {@link Service} annotation.</p>
 *
 * @author Dharshan K S (dharshan.ks@wipro.com)
 * @version 1.0
 * @since 1.0
 * @see CustomUserDetails
 * @see AppUserRepository
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Repository used to look up {@link AppUser} entities from the underlying data store.
     */
    private final AppUserRepository appUserRepository;

    /**
     * Constructs a new {@code CustomUserDetailsService} with the given {@link AppUserRepository}.
     *
     * <p>Spring injects the repository bean automatically through constructor-based
     * dependency injection when this service is instantiated.</p>
     *
     * @param appUserRepository the repository used to query {@link AppUser} records;
     *                          must not be {@code null}
     */
    public CustomUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    /**
     * Locates a user by their username and returns a fully populated {@link UserDetails} object.
     *
     * <p>This method is invoked by Spring Security's authentication mechanism during login.
     * It queries {@link AppUserRepository#findByUsername(String)} for an {@link AppUser}
     * matching the provided username. If no matching user is found, a
     * {@link UsernameNotFoundException} is thrown, causing authentication to fail with an
     * appropriate error response. On success, the {@link AppUser} is wrapped in a
     * {@link CustomUserDetails} instance which provides the user's password hash,
     * username, and role-based granted authorities to the security framework.</p>
     *
     * @param username the username identifying the user whose data is required;
     *                 must not be {@code null} or empty
     * @return a {@link CustomUserDetails} instance wrapping the found {@link AppUser},
     *         containing the user's credentials and granted authorities
     * @throws UsernameNotFoundException if no user with the given {@code username} exists
     *                                   in the data store
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new CustomUserDetails(user);
    }
}
