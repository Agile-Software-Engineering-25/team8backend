package com.ase.userservice.controllers;

import com.ase.userservice.controllers.dto.BulkUserIdsRequest;
import com.ase.userservice.controllers.dto.ChangeRoleRequest;
import com.ase.userservice.controllers.dto.ChangeRoleResponse;
import com.ase.userservice.services.RoleAssignmentService;
import com.ase.userservice.services.RoleMetadataService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RoleAssignmentController {

  private final RoleAssignmentService assignmentService;
  private final RoleMetadataService metaService;

  public RoleAssignmentController(RoleAssignmentService assignmentService, RoleMetadataService metaService) {
    this.assignmentService = assignmentService;
    this.metaService = metaService;
  }

  // Einzelzuweisung
  @PostMapping("/users/{userId}/roles/{role}")
  public ResponseEntity<Void> assign(@PathVariable String userId, @PathVariable("role") String roleName) {
    assignmentService.assignRoleToUser(userId, roleName);
    return ResponseEntity.ok().build();
  }

  // Einzelentzug (mit Fallback-Common, falls nötig)
  @DeleteMapping("/users/{userId}/roles/{role}")
  public ResponseEntity<Void> unassign(@PathVariable String userId, @PathVariable("role") String roleName) {
    assignmentService.removeRoleFromUser(userId, roleName);
    return ResponseEntity.noContent().build();
  }

  // Bulk-Zuweisung
  @PostMapping("/roles/{role}/assign")
  public ResponseEntity<Integer> bulkAssign(@PathVariable("role") String roleName,
                                            @Valid @RequestBody BulkUserIdsRequest body) {
    int applied = assignmentService.bulkAssign(roleName, body.userIds());
    return ResponseEntity.ok(applied);
  }

  // Bulk-Entzug (mit Fallback)
  @PostMapping("/roles/{role}/unassign")
  public ResponseEntity<Integer> bulkUnassign(@PathVariable("role") String roleName,
                                              @Valid @RequestBody BulkUserIdsRequest body) {
    int applied = assignmentService.bulkUnassign(roleName, body.userIds());
    return ResponseEntity.ok(applied);
  }

  // Rollenwechsel innerhalb Standardrollen + Rückgabe der benötigten Zusatzfelder
  @PostMapping("/users/roles/change")
  public ResponseEntity<ChangeRoleResponse> changeRole(@Valid @RequestBody ChangeRoleRequest req) {
    int applied = assignmentService.changeStandardRole(req.userIds(), req.targetRole());
    var fields = metaService.getRequiredFields(req.targetRole());
    return ResponseEntity.ok(new ChangeRoleResponse(applied, fields));
  }
}
