package com.foodrescue.config;

import com.foodrescue.controller.AuthController;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI foodRescueOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("FoodRescue API")
                .version("1.0.0")
                .description("API for surplus food donation, claims, deliveries, and notifications."))
            .components(new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                ));
    }

    @Bean
    OperationCustomizer bearerAuthOperationCustomizer() {
        return (operation, handlerMethod) -> {
            if (!AuthController.class.isAssignableFrom(handlerMethod.getBeanType())) {
                operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
            }
            return operation;
        };
    }
}
