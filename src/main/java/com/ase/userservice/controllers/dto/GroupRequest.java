package com.ase.userservice.controllers.dto;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record GroupRequest(@NotBlank String name,
                           Map<String, String> attributes) {}
