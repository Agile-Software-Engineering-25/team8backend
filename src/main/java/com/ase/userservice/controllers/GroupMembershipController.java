package com.ase.userservice.controllers;

import com.ase.userservice.controllers.dto.BulkUserIdsRequest;
import com.ase.userservice.controllers.dto.UserDto;
import com.ase.userservice.services.KeycloakGroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ase-08")
public class GroupMembershipController {

  private final KeycloakGroupService groupService;

  public GroupMembershipController(KeycloakGroupService groupService) {
    this.groupService = groupService;
  }

  /* Users einer Gruppe anzeigen */
  @GetMapping("/groups/{groupId}/users")
  public ResponseEntity<List<UserDto>> getGroupMembers(
      @PathVariable String groupId,
      @RequestParam(defaultValue = "0") int first,
      @RequestParam(defaultValue = "100") int max) {

    return ResponseEntity.ok(groupService.getGroupMembers(groupId, first, max));
  }

  /* Bulk: Users einer Gruppe hinzufügen / entfernen */
  @PostMapping("/groups/{groupId}/users")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void addUsersToGroup(@PathVariable String groupId,
                              @RequestBody BulkUserIdsRequest body) {
    groupService.addUsersToGroup(groupId, body);
  }

  @DeleteMapping("/groups/{groupId}/users")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeUsersFromGroup(@PathVariable String groupId,
                                   @RequestBody BulkUserIdsRequest body) {
    groupService.removeUsersFromGroup(groupId, body);
  }

  /* Single User ↔ Group */
  @PostMapping("/users/{userId}/groups/{groupId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void addSingleUser(@PathVariable String userId,
                            @PathVariable String groupId) {
    groupService.addSingleUserToGroup(groupId, userId);
  }

  @DeleteMapping("/users/{userId}/groups/{groupId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeSingleUser(@PathVariable String userId,
                               @PathVariable String groupId) {
    groupService.removeSingleUserFromGroup(groupId, userId);
  }
}
