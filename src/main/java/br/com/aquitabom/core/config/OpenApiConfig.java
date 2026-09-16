package br.com.aquitabom.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "Aqui Tá Bom API",
                version = "v1",
                description = "Rede social de feedbacks de refeições para restaurantes do Recife Antigo (MVP)"))
@SecurityScheme(
        name = "bearer-jwt",
        type = io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Token JWT obtido em /v1/api/auth/login, enviado como 'Authorization: Bearer <token>'")
public class OpenApiConfig {
}
