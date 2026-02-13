package com.example.subscription_platform.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "Subscription Management Platform API", version = "1.0.0", description = "REST API for managing subscriptions, payments, and user accounts. Supports JWT authentication and role-based authorization.", contact = @Contact(name = "API Support", email = "support@example.com")), servers = {
        @Server(url = "http://localhost:8080", description = "Local Development Server")
})
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", description = "Enter JWT token obtained from /auth/login or /auth/register")
public class OpenApiConfig {
    // Configuration is handled via annotations
}
