package com.ase.userservice.controllers.dto;

public record GroupDto(
    String id,
    String name,
    long memberCount
) {}
