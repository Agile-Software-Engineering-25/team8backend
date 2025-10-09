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

  private String serverUrl;        // z. B. https://keycloak.sau-protal.de
  private String realm;            // z. B. sau
  private String clientId;         // z. B. admin-cli
  private String username;         // ← NEU: Admin-User (Password Grant)
  private String password;         // ← NEU: Admin-Password
  private String clientSecret;     // optional (nur falls client confidential ist)

  private Integer connectTimeoutMs = 10000;
  private Integer readTimeoutMs = 20000;

}
