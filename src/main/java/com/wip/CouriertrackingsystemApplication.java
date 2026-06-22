package com.wip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Courier Tracking System Spring Boot application.
 *
 * <p>This class bootstraps the entire Spring application context using
 * {@link SpringBootApplication}, which enables auto-configuration, component
 * scanning, and configuration property binding. On successful startup, a
 * confirmation message is printed to standard output.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
public class CouriertrackingsystemApplication {

    /**
     * The main method that serves as the JVM entry point.
     *
     * <p>Delegates to {@link SpringApplication#run(Class, String[])} to launch
     * the embedded web server, initialize the Spring IoC container, and wire all
     * application beans. After the context is fully started, a success message is
     * printed to standard output.</p>
     *
     * @param args command-line arguments passed to the application at startup
     */
    public static void main(String[] args) {
        SpringApplication.run(CouriertrackingsystemApplication.class, args);
        System.out.println("Application Started Successfully");
    }
}
