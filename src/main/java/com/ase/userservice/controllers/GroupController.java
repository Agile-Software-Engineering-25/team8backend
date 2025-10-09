package com.ase.userservice.controllers;

import com.ase.userservice.controllers.dto.*;
import com.ase.userservice.services.GroupRoleService;
import com.ase.userservice.services.GroupService;
import jakarta.validation.Valid;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

  private final GroupService groupService;
  private final GroupRoleService groupRoleService;

  public GroupController(GroupService groupService, GroupRoleService groupRoleService) {
    this.groupService = groupService; this.groupRoleService = groupRoleService;
  }

  @PostMapping
  public ResponseEntity<Void> create(@Valid @RequestBody GroupRequest req, UriComponentsBuilder uri) {
    groupService.create(req);
    return ResponseEntity.created(uri.path("/api/groups").build().toUri()).build();
  }

  @GetMapping
  public List<GroupRepresentation> list() { return groupService.list(); }

  @GetMapping("/{groupId}")
  public GroupRepresentation get(@PathVariable String groupId) { return groupService.get(groupId); }

  @PutMapping("/{groupId}")
  public ResponseEntity<Void> update(@PathVariable String groupId, @RequestBody GroupUpdateRequest req) {
    groupService.update(groupId, req);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{groupId}")
  public ResponseEntity<Void> delete(@PathVariable String groupId) {
    groupService.delete(groupId);
    return ResponseEntity.noContent().build();
  }

  // Rollen (nur zuordnen/entfernen, keine Rollen-CRUD)
  @GetMapping("/{groupId}/roles")
  public List<RoleRepresentation> listRoles(@PathVariable String groupId) {
    return groupRoleService.listRealmRoles(groupId);
  }

  @PostMapping("/{groupId}/roles")
  public ResponseEntity<Void> addRoles(@PathVariable String groupId, @Valid @RequestBody RoleNamesRequest req) {
    groupRoleService.addRealmRoles(groupId, req);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{groupId}/roles")
  public ResponseEntity<Void> removeRoles(@PathVariable String groupId, @Valid @RequestBody RoleNamesRequest req) {
    groupRoleService.removeRealmRoles(groupId, req);
    return ResponseEntity.noContent().build();
  }
}
