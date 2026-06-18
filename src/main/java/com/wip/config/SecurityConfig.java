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

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider());

        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/error", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/", "/home").hasAnyRole("USER", "ADMIN")

                        .requestMatchers("/customers", "/customers/", "/customers/new", "/customers/save").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/customers/*").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/customers/*/edit", "/customers/*/update", "/customers/*/delete").hasRole("ADMIN")

                        .requestMatchers("/parcels", "/parcels/", "/parcels/new", "/parcels/save").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/parcels/*").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/parcels/*/edit", "/parcels/*/update", "/parcels/*/delete").hasRole("ADMIN")

                        .requestMatchers("/shipments", "/shipments/", "/shipments/*").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/shipments/new", "/shipments/save", "/shipments/*/edit", "/shipments/*/update", "/shipments/*/delete").hasRole("ADMIN")

                        .requestMatchers("/tracking", "/tracking/shipment/*").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/tracking/shipment/*/new", "/tracking/shipment/*/save", "/tracking/update/*/delete").hasRole("ADMIN")

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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}