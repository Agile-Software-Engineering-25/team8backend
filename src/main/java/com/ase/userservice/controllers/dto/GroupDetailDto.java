package com.ase.userservice.controllers.dto;

import java.util.List;

public record GroupDetailDto(
    String id,
    String name,
    java.util.Map<String, java.util.List<String>> attributes,
    List<String> permissionIds
) {}
