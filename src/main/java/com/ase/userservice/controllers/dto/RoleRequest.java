package com.ase.userservice.controllers.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(
    @NotBlank String name,
    String description
) {}
