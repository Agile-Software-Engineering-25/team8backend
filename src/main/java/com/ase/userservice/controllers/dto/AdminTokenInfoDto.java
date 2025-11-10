package com.ase.userservice.controllers.dto;

public record AdminTokenInfoDto(
    String tokenType,
    long expiresIn,
    long refreshExpiresIn,
    String scope
) {}
