package com.wip.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration class that enables Cross-Origin Resource Sharing (CORS).
 *
 * <p>This configuration implements {@link WebMvcConfigurer} to globally permit cross-origin
 * HTTP requests from any origin ({@code *}) for all application endpoints. It allows the
 * standard HTTP methods ({@code GET}, {@code POST}, {@code PUT}, {@code DELETE}), making
 * the REST API accessible to browser-based clients hosted on different domains or ports
 * during development and production.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Registers global CORS mappings for all endpoints in the application.
     *
     * <p>Applies a wildcard path pattern ({@code /**}) so that every endpoint
     * is covered. All origins are permitted via the {@code *} wildcard, and the
     * allowed HTTP methods are {@code GET}, {@code POST}, {@code PUT}, and
     * {@code DELETE}.</p>
     *
     * @param registry the {@link CorsRegistry} used to configure CORS mappings
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
