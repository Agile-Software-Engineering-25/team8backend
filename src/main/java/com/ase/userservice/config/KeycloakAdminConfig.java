package com.ase.userservice.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class KeycloakAdminConfig {

  @Value("${keycloak.server-url}")
  private String serverUrl;

  @Value("${keycloak.realm}")
  private String realm;

  @Value("${keycloak.client-id}")
  private String clientId;

  @Value("${keycloak.client-secret}")
  private String clientSecret;

  @Value("${cors.allowed-origins}")
  private String allowedOrigins;

  // 1) Keycloak-Admin-Client: holt/refresh’t den Access Token automatisch
  @Bean
  public Keycloak keycloak() {
    return KeycloakBuilder.builder()
        .serverUrl(serverUrl)
        .realm(realm)
        .clientId(clientId)
        .clientSecret(clientSecret)
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .build();
  }

  // 2) RealmResource als zentrale Bean, die alle Services nutzen
  @Bean
  public RealmResource realmResource(Keycloak keycloak) {
    return keycloak.realm(realm);
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
}
