package com.ase.userservice.controllers;

import com.ase.userservice.controllers.dto.RoleSummary;
import com.ase.userservice.controllers.dto.UserSummary;
import com.ase.userservice.services.RoleQueryService;
import com.ase.userservice.services.UserService;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

  private final UserService userService;
  private final RoleQueryService roleQueryService;

  public UserController(UserService userService, RoleQueryService roleQueryService) {
    this.userService = userService;
    this.roleQueryService = roleQueryService;
  }

  @GetMapping
  public List<UserSummary> search(@RequestParam(name = "q", required = false) String q,
                                  @RequestParam(name = "first", required = false) Integer first,
                                  @RequestParam(name = "max", required = false) Integer max) {
    List<UserRepresentation> users = userService.search(q, first, max);
    return users.stream().map(UserController::toSummary).toList();
  }

  @GetMapping("/{userId}")
  public UserSummary get(@PathVariable String userId) {
    return toSummary(userService.getById(userId));
  }

  @GetMapping("/{userId}/roles")
  public java.util.List<RoleSummary> directRoles(@PathVariable String userId) {
    var roles = roleQueryService.getUserDirectRealmRoles(userId);
    return roles.stream()
        .map(r -> new RoleSummary(
            r.getName(),
            r.getDescription(),
            r.isComposite(),
            java.util.List.of("direct")
        ))
        .toList();
  }

  @GetMapping("/{userId}/roles/effective")
  public java.util.List<RoleSummary> effectiveRoles(@PathVariable String userId) {
    return roleQueryService.getUserEffectiveRoleSummaries(userId);
  }

  private static UserSummary toSummary(UserRepresentation u) {
    return new UserSummary(
        u.getId(),
        u.getUsername(),
        u.getEmail(),
        u.getFirstName(),
        u.getLastName(),
        Boolean.TRUE.equals(u.isEnabled())
    );
  }

  private static RoleSummary toSummary(org.keycloak.representations.idm.RoleRepresentation r) {
    return new RoleSummary(
        r.getName(),
        r.getDescription(),
        r.isComposite(),
        java.util.List.of("direct")
    );
  }
}
