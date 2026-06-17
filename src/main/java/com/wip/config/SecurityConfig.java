package com.wip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/error", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
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
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("user")
                .password(passwordEncoder.encode("user123"))
                .roles("USER")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}