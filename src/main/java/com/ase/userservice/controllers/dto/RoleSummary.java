package com.ase.userservice.controllers.dto;

import java.util.List;

public record RoleSummary(
    String name,
    String description,
    boolean composite,
    List<String> sources
) {}
