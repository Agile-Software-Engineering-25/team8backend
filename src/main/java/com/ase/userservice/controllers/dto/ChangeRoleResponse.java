package com.ase.userservice.controllers.dto;

import java.util.List;

public record ChangeRoleResponse(
    int appliedTo,
    List<String> requiredFields
) {}
