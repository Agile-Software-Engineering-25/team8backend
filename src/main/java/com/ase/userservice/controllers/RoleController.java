package com.ase.userservice.controllers;

import com.ase.userservice.controllers.dto.*;
import com.ase.userservice.services.KeycloakRoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ase-08")
public class RoleController {

    private final KeycloakRoleService roleService;

    public RoleController(KeycloakRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAllRoles());
    }

    @GetMapping("/roles/{roleId}")
    public ResponseEntity<RoleDetailDto> getRoleById(@PathVariable String roleId) {
        return ResponseEntity.ok(roleService.findRoleById(roleId));
    }

    @GetMapping("/roles/search")
    public ResponseEntity<List<RoleDto>> searchRoles(@RequestParam String name) {
      return ResponseEntity.ok(roleService.searchRolesByName(name));
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    public void createRole(@RequestBody CreateRoleRequest request) {
        roleService.createRole(request);
    }

    @PutMapping("/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRole(@PathVariable String roleId, @RequestBody UpdateRoleRequest request) {
        roleService.updateRole(roleId, request);
    }

    @PostMapping("/roles/{roleId}/permissions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addPermission(@PathVariable String roleId, @RequestBody Map<String, String> payload) {
        String permissionId = payload.get("permissionId");
        roleService.addPermissionToRole(roleId, permissionId);
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePermission(@PathVariable String roleId, @PathVariable String permissionId) {
        roleService.removePermissionFromRole(roleId, permissionId);
    }

    @GetMapping("/standard-roles")
    public ResponseEntity<List<String>> getStandardRoles() {
        return ResponseEntity.ok(roleService.getStandardRoles());
    }
}
