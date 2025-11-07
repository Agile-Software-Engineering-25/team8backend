package com.ase.userservice.controllers;

import com.ase.userservice.controllers.dto.PermissionDto;
import com.ase.userservice.controllers.dto.UserDto;
import com.ase.userservice.services.KeycloakGroupService;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/ase-08/users")
public class UserController {

  private final KeycloakGroupService groupService;

  public UserController(KeycloakGroupService groupService) {
    this.groupService = groupService;
  }

  /* Suche */
  @GetMapping
  public ResponseEntity<List<UserDto>> searchUsers(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int first,
      @RequestParam(defaultValue = "50") int max) {

    return ResponseEntity.ok(groupService.searchUsers(q, first, max));
  }

  /* User-Details */
  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> getUser(@PathVariable String userId) {
    return ResponseEntity.ok(groupService.getUserById(userId));
  }

  @GetMapping("/search")
  public ResponseEntity<List<UserDto>> searchUsersByName(
      @RequestParam String name,
      @RequestParam(defaultValue = "0") int first,
      @RequestParam(defaultValue = "50") int max) {

    return ResponseEntity.ok(groupService.searchUsers(name, first, max));
  }

  /* Gruppen eines Users */
  @GetMapping("/{userId}/groups")
  public ResponseEntity<List<GroupRepresentation>> getUserGroups(@PathVariable String userId) {
    var groups = groupService.getRealm()
        .users().get(userId).groups(); // wenn du Realm nicht exposen willst, weglassen
    return ResponseEntity.ok(groups);
  }

  /* Effektive Berechtigungen eines Users */
  @GetMapping("/{userId}/permissions")
  public ResponseEntity<List<PermissionDto>> getUserPermissions(@PathVariable String userId) {
    return ResponseEntity.ok(groupService.getUserEffectivePermissions(userId));
  }
}
