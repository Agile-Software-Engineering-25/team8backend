package com.ase.userservice.controllers.dto;

import java.util.Map;

public record CreateGroupRequest(
    String name,
    Map<String, String> attributes
) {}
