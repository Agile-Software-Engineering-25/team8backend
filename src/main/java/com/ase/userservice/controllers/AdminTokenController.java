package com.ase.userservice.controllers;

import com.ase.userservice.controllers.dto.AdminTokenInfoDto;
import com.ase.userservice.services.KeycloakAdminTokenService;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ase-08/admin")
public class AdminTokenController {

  private final KeycloakAdminTokenService tokenService;

  public AdminTokenController(KeycloakAdminTokenService tokenService) {
    this.tokenService = tokenService;
  }

  @GetMapping("/token-info")
  public ResponseEntity<AdminTokenInfoDto> tokenInfo() {
    AccessTokenResponse t = tokenService.currentToken();
    AdminTokenInfoDto dto = new AdminTokenInfoDto(
        t.getTokenType(),
        t.getExpiresIn(),
        t.getRefreshExpiresIn(),
        t.getScope()
    );
    return ResponseEntity.ok(dto);
  }
}
