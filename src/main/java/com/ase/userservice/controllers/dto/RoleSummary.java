package com.ase.userservice.controllers.dto;

public record RoleSummary(
    String name,
    String description,
    boolean composite
) {}
