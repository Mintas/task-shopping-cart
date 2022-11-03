package ru.kovalev.shopping.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@SecurityScheme(
        name = SwaggerConfig.SECURED,
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
@OpenAPIDefinition(info = @Info(
        title = "Shopping Cart API",
        description = "Shopping Cart :: DEMO",
        version = "v1"))
@Profile("test")
@Configuration
public class TestSwaggerConfig {
}
