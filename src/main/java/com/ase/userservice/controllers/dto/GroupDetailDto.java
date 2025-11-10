package com.ase.userservice.controllers.dto;

import java.util.List;
import java.util.Map;

public record GroupDetailDto(
    String id,
    String name,
    Map<String, List<String>> attributes,
    List<String> permissionIds,
    String parentName,
    List<GroupRefDto> ancestors,
    List<GroupRefDto> children
) {}
