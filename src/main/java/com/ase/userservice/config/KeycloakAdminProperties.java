package com.ase.userservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "keycloak.admin")
public class KeycloakAdminProperties {

  private String serverUrl;
  private String realm;
  private String clientId;
  private String username;
  private String password;
  private String clientSecret;

  private Integer connectTimeoutMs = 10000;
  private Integer readTimeoutMs = 20000;

}
