package com.ase.userservice.controllers.dto;

public record UserSummary(
    String id,
    String username,
    String email,
    String firstName,
    String lastName,
    boolean enabled
) {}
