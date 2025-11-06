package com.ase.userservice.controllers.dto;

import java.util.List;

public record GroupPermissionsRequest(
    List<String> permissionIds
) {}
