package com.wip.config;

import com.wip.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for the Courier Tracking System.
 *
 * <p>This class configures the complete security posture of the application using
 * Spring Security 6. It defines the authentication mechanism via a
 * {@link DaoAuthenticationProvider} backed by {@link CustomUserDetailsService},
 * sets up BCrypt-based password encoding, and establishes the HTTP security
 * filter chain with:</p>
 * <ul>
 *   <li>Role-based URL authorization distinguishing {@code ADMIN} and {@code USER} roles.</li>
 *   <li>Form-login with a custom login page at {@code /login} and redirect to {@code /} on success.</li>
 *   <li>Session invalidation, authentication clearing, and JSESSIONID cookie deletion on logout.</li>
 *   <li>CSRF protection disabled only for REST API endpoints under {@code /api/**}.</li>
 *   <li>HTTP Basic authentication enabled as a fallback for API clients.</li>
 * </ul>
 *
 * @author Dharshan K S (dharshan.ks@wipro.com)
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class SecurityConfig {

    /**
     * The custom {@link org.springframework.security.core.userdetails.UserDetailsService}
     * used to load user-specific data during authentication.
     */
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Constructs a new {@code SecurityConfig} with the required {@link CustomUserDetailsService}.
     *
     * <p>Spring automatically injects the {@link CustomUserDetailsService} bean into
     * this constructor through constructor-based dependency injection.</p>
     *
     * @param customUserDetailsService the service responsible for loading user details
     *                                 from the data store during the authentication process;
     *                                 must not be {@code null}
     */
    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Creates and configures a {@link DaoAuthenticationProvider} bean.
     *
     * <p>The {@link DaoAuthenticationProvider} integrates with {@link CustomUserDetailsService}
     * to retrieve user credentials and role information from the database, and uses the
     * {@link #passwordEncoder()} bean to verify the supplied raw password against the
     * stored BCrypt-encoded hash during the authentication process.</p>
     *
     * @return a fully configured {@link DaoAuthenticationProvider} that delegates
     *         user lookup to {@link CustomUserDetailsService} and password verification
     *         to the {@link BCryptPasswordEncoder}
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Builds and returns the primary {@link SecurityFilterChain} bean for the application.
     *
     * <p>This method configures the HTTP security rules in the following order:</p>
     * <ol>
     *   <li><strong>Authentication Provider:</strong> Registers the {@link DaoAuthenticationProvider}.</li>
     *   <li><strong>CSRF:</strong> Disabled for all paths matching {@code /api/**} to support
     *       stateless REST API clients; enabled for all other paths.</li>
     *   <li><strong>Authorization:</strong>
     *     <ul>
     *       <li>Public paths (login, register, static resources, Swagger UI, Actuator) are
     *           accessible without authentication.</li>
     *       <li>Dashboard, profile, parcels, shipments (read), tracking (read), and search
     *           endpoints require either the {@code USER} or {@code ADMIN} role.</li>
     *       <li>Customer management, shipment management (write/delete), tracking management
     *           (write/delete), and all {@code /api/**} endpoints require the {@code ADMIN} role.</li>
     *       <li>All other requests require the user to be authenticated.</li>
     *     </ul>
     *   </li>
     *   <li><strong>Form Login:</strong> Custom login page at {@code /login}, processing URL
     *       at {@code /login}, and redirects to {@code /} on successful authentication.</li>
     *   <li><strong>Logout:</strong> Logout URL at {@code /logout}; on success, redirects to
     *       {@code /login?logout}, invalidates the HTTP session, clears authentication, and
     *       deletes the {@code JSESSIONID} cookie.</li>
     *   <li><strong>HTTP Basic:</strong> Enabled with default settings for API consumers.</li>
     * </ol>
     *
     * @param http the {@link HttpSecurity} object provided by Spring Security to configure
     *             the security filter chain; must not be {@code null}
     * @return the built {@link SecurityFilterChain} that enforces all configured security rules
     * @throws Exception if an error occurs while building the {@link HttpSecurity} configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider());

        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/error", "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/swagger-ui.html", "/actuator/**").permitAll()
                        .requestMatchers("/", "/dashboard").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/home").permitAll()
                        .requestMatchers("/profile", "/profile/edit", "/profile/update").hasAnyRole("USER", "ADMIN")

                        .requestMatchers("/customers", "/customers/*", "/customers/*/edit", "/customers/*/update", "/customers/*/delete").hasRole("ADMIN")

                        .requestMatchers("/parcels", "/parcels/new", "/parcels/save", "/parcels/*").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/parcels/*/edit", "/parcels/*/update", "/parcels/*/delete").hasAnyRole("USER", "ADMIN")

                        .requestMatchers("/shipments", "/shipments/*").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/shipments/new", "/shipments/save", "/shipments/*/edit", "/shipments/*/update", "/shipments/*/delete").hasRole("ADMIN")

                        .requestMatchers("/tracking", "/tracking/shipment/*").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/tracking/shipment/*/new", "/tracking/shipment/*/save", "/tracking/update/*/delete").hasRole("ADMIN")

                        .requestMatchers("/search").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * Creates and returns a {@link PasswordEncoder} bean backed by the BCrypt hashing algorithm.
     *
     * <p>BCrypt is a strong, adaptive, salted hashing algorithm recommended by OWASP for
     * password storage. The encoder automatically generates a random salt and embeds it in
     * the resulting hash, making each encoded password unique even for identical raw inputs.
     * This bean is used both by the {@link #authenticationProvider()} during login verification
     * and by the registration service when persisting new user passwords.</p>
     *
     * @return a {@link BCryptPasswordEncoder} instance configured with Spring Security's
     *         default BCrypt strength factor (10 rounds)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
