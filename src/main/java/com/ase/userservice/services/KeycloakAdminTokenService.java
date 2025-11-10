package com.ase.userservice.services;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.stereotype.Service;

@Service
public class KeycloakAdminTokenService {

  private final Keycloak keycloak;

  public KeycloakAdminTokenService(Keycloak keycloak) {
    this.keycloak = keycloak;
  }

  /**
   * Liefert das aktuelle AccessTokenResponse-Objekt des Admin-Clients (team-8).
   * Keycloak kümmert sich um Holen/Refresh.
   */
  public AccessTokenResponse currentToken() {
    return keycloak.tokenManager().getAccessToken();
  }

  /**
   * Nur der Token-String (Bearer-Value), falls du ihn brauchst
   * z.B. für eigene RestTemplate/WebClient-Calls.
   */
  public String getTokenValue() {
    return currentToken().getToken();
  }
}
