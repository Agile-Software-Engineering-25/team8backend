package com.ase.userservice.controllers.dto;

import java.util.List;
import java.util.Map;
import com.ase.userservice.controllers.dto.PermissionDto;

public record GroupDetailDto(
    String id,
    String name,
    Map<String, List<String>> attributes,
    List<PermissionDto> permissions,
    String parentName,
    List<GroupRefDto> ancestors,
    List<GroupRefDto> children
) {}
