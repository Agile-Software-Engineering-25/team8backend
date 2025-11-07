package com.ase.userservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI userserviceOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Userservice API – Groups & Roles")
            .description("Verwaltung von Keycloak-Gruppen, Berechtigungen und Benutzern")
            .version("v1"));
  }

  @Bean
  public GroupedOpenApi ase08Api() {
    return GroupedOpenApi.builder()
        .group("ase-08")
        .pathsToMatch("/api/**")
        .build();
  }
}
