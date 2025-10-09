package com.ase.userservice.controllers;

import com.ase.userservice.controllers.dto.BulkUserIdsRequest;
import com.ase.userservice.services.GroupMembershipService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GroupMembershipController {

  private final GroupMembershipService svc;
  public GroupMembershipController(GroupMembershipService svc) { this.svc = svc; }

  @PostMapping("/users/{userId}/groups/{groupId}")
  public ResponseEntity<Void> addSingle(@PathVariable String userId, @PathVariable String groupId) {
    svc.addUser(userId, groupId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/users/{userId}/groups/{groupId}")
  public ResponseEntity<Void> removeSingle(@PathVariable String userId, @PathVariable String groupId) {
    svc.removeUser(userId, groupId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/groups/{groupId}/users")
  public ResponseEntity<Integer> addBulk(@PathVariable String groupId, @Valid @RequestBody BulkUserIdsRequest body) {
    return ResponseEntity.ok(svc.addUsersBulk(groupId, body));
  }

  @DeleteMapping("/groups/{groupId}/users")
  public ResponseEntity<Integer> removeBulk(@PathVariable String groupId, @Valid @RequestBody BulkUserIdsRequest body) {
    return ResponseEntity.ok(svc.removeUsersBulk(groupId, body));
  }
}
