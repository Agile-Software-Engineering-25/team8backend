package com.ase.userservice.controllers.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ChangeRoleRequest(
    @NotEmpty List<String> userIds,
    @NotBlank String targetRole
) {}
