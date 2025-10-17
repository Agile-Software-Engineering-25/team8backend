package com.example.keycloak.backend.dto;

import java.util.List;

// Für die Detailansicht einer Rolle
public record RoleDetailDto(String id, String name, List<String> assignedPermissionIds) {}
