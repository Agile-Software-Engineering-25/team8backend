package com.ase.userservice.controllers.dto;

public record UserDto(
    String id,
    String username,
    String email,
    boolean enabled
) {}
