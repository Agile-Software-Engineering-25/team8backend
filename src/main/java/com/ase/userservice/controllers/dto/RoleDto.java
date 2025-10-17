package com.example.keycloak.backend.dto;

// Für die Hauptansicht (Liste aller Rollen)
public record RoleDto(String id, String name, String standardRole, long userCount) {}
