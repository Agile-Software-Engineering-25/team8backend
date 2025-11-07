package com.ase.userservice.controllers;

import com.ase.userservice.controllers.dto.*;
import com.ase.userservice.services.KeycloakGroupService;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/ase-08/groups")
public class GroupController {

  private final KeycloakGroupService groupService;

  public GroupController(KeycloakGroupService groupService) {
    this.groupService = groupService;
  }

  /* --------- CRUD Groups --------- */

  @GetMapping
  public ResponseEntity<List<GroupDto>> getAllGroups() {
    return ResponseEntity.ok(groupService.findAllGroups());
  }

  @GetMapping("/{groupId}")
  public ResponseEntity<GroupDetailDto> getGroupById(@PathVariable String groupId) {
    return ResponseEntity.ok(groupService.findGroupById(groupId));
  }

  @GetMapping("/search")
  public ResponseEntity<List<GroupDto>> searchGroups(@RequestParam String name) {
    return ResponseEntity.ok(groupService.searchGroupsByName(name));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public void createGroup(@RequestBody CreateGroupRequest request) {
    groupService.createGroup(request);
  }

  @PutMapping("/{groupId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateGroup(@PathVariable String groupId,
                          @RequestBody UpdateGroupRequest request) {
    groupService.updateGroup(groupId, request);
  }

  @DeleteMapping("/{groupId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteGroup(@PathVariable String groupId) {
    groupService.deleteGroup(groupId);
  }

  /* --------- Permissions einer Group --------- */

  @GetMapping("/{groupId}/permissions")
  public ResponseEntity<List<RoleRepresentation>> getGroupPermissions(@PathVariable String groupId) {
    return ResponseEntity.ok(groupService.findGroupPermissions(groupId));
  }

  @PostMapping("/{groupId}/permissions")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void addPermissions(@PathVariable String groupId,
                             @RequestBody GroupPermissionsRequest request) {
    groupService.addPermissionsToGroup(groupId, request);
  }

  @DeleteMapping("/{groupId}/permissions")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removePermissions(@PathVariable String groupId,
                                @RequestBody GroupPermissionsRequest request) {
    groupService.removePermissionsFromGroup(groupId, request);
  }
}
