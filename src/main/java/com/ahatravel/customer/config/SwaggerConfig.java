package com.ahatravel.customer.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AHA Travel Customer Portal API",
                version = "1.0",
                description = "Production-grade REST API for the AHA Travel Customer Portal",
                contact = @Contact(name = "AHA Travel", email = "support@ahatravel.com")
        ),
        servers = @Server(url = "/api/v1", description = "Local Development Server")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "JWT access token (Bearer)"
)
public class SwaggerConfig {
}
