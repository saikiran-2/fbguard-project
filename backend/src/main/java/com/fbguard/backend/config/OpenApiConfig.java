package com.fbguard.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Adds a title/description and a "bearer JWT" auth button to the
 * auto-generated Swagger UI at /swagger-ui.html, so reviewers can log in with
 * a token and try endpoints directly from the browser instead of Postman.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fbGuardOpenApi() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("FBGuard API")
                        .description("Malicious Facebook app detection platform - auth, apps, " +
                                "friends, messages, and admin endpoints.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components().addSecuritySchemes(schemeName,
                        new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
