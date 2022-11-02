package ru.kovalev.shopping.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

//@SecurityScheme(
//        name = SwaggerConfig.BASIC_AUTH,
//        type = SecuritySchemeType.HTTP,
//        scheme = "basic"
//)
@SecurityScheme(
        name = SwaggerConfig.OIDC,
        type = SecuritySchemeType.OPENIDCONNECT,
        scheme = "openIdConnect"
)
@OpenAPIDefinition(info = @Info(
        title = "Shopping Cart API",
        description = "Shopping Cart :: DEMO",
        version = "v1"))
@Configuration
public class SwaggerConfig {
    public static final String OIDC = "OIDC";
    public static final String BASIC_AUTH = "basicAuth";
    public static final String SCOPE_OPENID = "openid";
}
