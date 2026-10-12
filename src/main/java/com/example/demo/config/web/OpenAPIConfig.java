package com.example.demo.config.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration
 *
 * Features:
 * - API documentation
 * - Interactive API explorer
 * - JWT authentication support
 * - Multiple servers configuration
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Configuration
public class OpenAPIConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${app.version:1.0.0}")
    private String version;

    @Bean
    public OpenAPI customOpenAPI() {
        log.info("Configuring OpenAPI documentation");

        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .version(version)
                        .description("Enterprise-grade Spring Boot application with CQRS and DDD")
                        .contact(new Contact()
                                .name("API Support Team")
                                .email("api-support@example.com")
                                .url("https://example.com/support"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))

                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api-staging.example.com")
                                .description("Staging Server"),
                        new Server()
                                .url("https://api.example.com")
                                .description("Production Server")
                ))

                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))

                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT authentication token"))
                );
    }
}