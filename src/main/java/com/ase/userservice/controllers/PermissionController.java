package com.example.keycloak.backend.controller;

import com.example.keycloak.backend.dto.PermissionDto;
import com.example.keycloak.backend.service.KeycloakRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    
    private final KeycloakRoleService roleService;

    public PermissionController(KeycloakRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<PermissionDto>> getAllPermissions() {
        return ResponseEntity.ok(roleService.findAllPermissions());
    }
}
