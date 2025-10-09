package com.ase.userservice.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {

  @Bean(destroyMethod = "close")
  public Keycloak keycloakAdmin(KeycloakAdminProperties props) {
    var b = KeycloakBuilder.builder()
        .serverUrl(props.getServerUrl())
        .realm(props.getRealm())
        .grantType(OAuth2Constants.PASSWORD)
        .clientId(props.getClientId())
        .username(props.getUsername())
        .password(props.getPassword());
    if (props.getClientSecret() != null && !props.getClientSecret().isBlank()) {
      b.clientSecret(props.getClientSecret());
    }
    return b.build();
  }
}
