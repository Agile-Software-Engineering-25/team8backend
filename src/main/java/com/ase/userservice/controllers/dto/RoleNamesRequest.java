package com.ase.userservice.controllers.dto;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RoleNamesRequest(@NotEmpty List<String> roleNames) {}
