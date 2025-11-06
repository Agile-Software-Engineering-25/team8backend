package com.ase.userservice.controllers.dto;

import java.util.Map;

public record UpdateGroupRequest(
    String name,
    Map<String, String> attributes
) {}
