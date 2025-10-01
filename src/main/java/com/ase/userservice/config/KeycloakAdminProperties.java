package com.ase.userservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "keycloak.admin")
public class KeycloakAdminProperties {
  private String serverUrl;
  private String realm;
  private String clientId;
  private String clientSecret;
  private Integer connectTimeoutMs = 10000;
  private Integer readTimeoutMs = 20000;

  public String getServerUrl() { return serverUrl; }
  public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
  public String getRealm() { return realm; }
  public void setRealm(String realm) { this.realm = realm; }
  public String getClientId() { return clientId; }
  public void setClientId(String clientId) { this.clientId = clientId; }
  public String getClientSecret() { return clientSecret; }
  public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
  public Integer getConnectTimeoutMs() { return connectTimeoutMs; }
  public void setConnectTimeoutMs(Integer connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
  public Integer getReadTimeoutMs() { return readTimeoutMs; }
  public void setReadTimeoutMs(Integer readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
}
