package com.wip.config;

import com.wip.entity.AppUser;
import com.wip.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds the database on startup.
 * Only creates the admin account if it does not already exist.
 * No demo/test data is created — the system starts clean for real use.
 */
@Configuration
public class DatabaseSeederConfig {

    @Bean
    public CommandLineRunner initDatabase(AppUserRepository appUserRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            // Only seed admin if it doesn't already exist (idempotent)
            if (appUserRepository.findByUsername("admin").isEmpty()) {
                AppUser adminUser = new AppUser();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setEmail("admin@nexuslogistics.com");
                adminUser.setRole("ADMIN");
                appUserRepository.save(adminUser);
                System.out.println("[Nexus] Admin account created. Username: admin / Password: admin123");
            } else {
                System.out.println("[Nexus] Admin account already exists. Skipping seed.");
            }
        };
    }
}
