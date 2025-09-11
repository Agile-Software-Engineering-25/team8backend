package com.ase.userservice.controllers.dto;

public record RoleUpdateRequest(
    String newName,
    String description
) {}
