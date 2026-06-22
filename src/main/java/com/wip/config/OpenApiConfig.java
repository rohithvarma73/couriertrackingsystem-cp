package com.wip.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class that sets up the OpenAPI 3 (Swagger) documentation.
 *
 * <p>This configuration registers a custom {@link OpenAPI} bean that populates the
 * Swagger UI with the application's title, description, and version. The generated
 * documentation is served at {@code /swagger-ui.html} and the OpenAPI spec at
 * {@code /v3/api-docs} by default when SpringDoc is on the classpath.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures a custom {@link OpenAPI} instance for the Courier Tracking System.
     *
     * <p>The returned bean is picked up by SpringDoc and used to populate the
     * Swagger UI with the application metadata, including title, a human-readable
     * description, and the current API version.</p>
     *
     * @return a fully configured {@link OpenAPI} instance with application info
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Courier Tracking System")
                        .description("REST API documentation for courier tracking system")
                        .version("v1.0"));
    }
}
