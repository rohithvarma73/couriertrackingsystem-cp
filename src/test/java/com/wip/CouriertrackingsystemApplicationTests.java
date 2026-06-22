package com.wip;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring Boot application context integration test for the Courier Tracking System.
 *
 * <p>Verifies that the entire Spring application context loads successfully under the
 * {@code test} profile. A failure in this test indicates a misconfiguration in bean
 * definitions, auto-configuration, or environment properties that prevents the application
 * from starting.</p>
 *
 * @author Dharshan K S
 * @version 1.0
 * @since 1.0
 */
@SpringBootTest
@ActiveProfiles("test")
class CouriertrackingsystemApplicationTests {

    /**
     * Validates that the Spring application context starts without any errors.
     *
     * <p>This smoke test ensures all beans are correctly wired, auto-configurations
     * are resolved, and the application is ready to serve requests when the
     * {@code test} profile is active.</p>
     */
    @Test
    void contextLoads() {
    }
}