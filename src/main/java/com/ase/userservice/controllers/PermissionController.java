package com.ase.userservice.controllers;

import com.ase.userservice.controllers.dto.PermissionDto;
import com.ase.userservice.services.KeycloakRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/ase-08/permissions")
public class PermissionController {

    private final KeycloakRoleService roleService;

    public PermissionController(KeycloakRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<PermissionDto>> getAllPermissions() {
        return ResponseEntity.ok(roleService.findAllPermissions());
    }

    @GetMapping("/search")
    public ResponseEntity<List<PermissionDto>> searchPermissions(@RequestParam String name) {
      return ResponseEntity.ok(roleService.searchPermissionsByName(name));
    }

    @GetMapping("/{permissionId}")
    public ResponseEntity<PermissionDto> getPermissionById(
        @PathVariable String permissionId) {
      return ResponseEntity.ok(roleService.findPermissionById(permissionId));
    }
}
