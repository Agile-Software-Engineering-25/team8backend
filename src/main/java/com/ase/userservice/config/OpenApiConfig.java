package com.ase.userservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

  @Value("${keycloak.auth-url}")
  private String authUrl;

  @Value("${keycloak.token-url}")
  private String tokenUrl;

  @Bean
  public OpenAPI userserviceOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Userservice API – Groups & Roles")
            .description("Verwaltung von Keycloak-Gruppen, Berechtigungen und Benutzern")
            .version("v1"))
        .components(new Components()
            .addSecuritySchemes("keycloak", new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows()
                    .authorizationCode(new OAuthFlow()
                        .authorizationUrl(authUrl)
                        .tokenUrl(tokenUrl)
                        .scopes(new Scopes()
                            .addString("openid", "OpenID Connect")
                            .addString("profile", "User profile"))))))
        // standardmäßig alle Endpoints mit diesem Scheme sichern
        .addSecurityItem(new SecurityRequirement().addList("keycloak", List.of("openid")));
  }

  // Gruppe "ase-08" -> /v3/api-docs/ase-08
  @Bean
  public GroupedOpenApi ase08Api() {
    return GroupedOpenApi.builder()
        .group("ase-08")
        .pathsToMatch("/api/**")
        .build();
  }
}
