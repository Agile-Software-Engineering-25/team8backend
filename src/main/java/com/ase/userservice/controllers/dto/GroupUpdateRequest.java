package com.ase.userservice.controllers.dto;
import java.util.Map;

public record GroupUpdateRequest(String name,
                                 Map<String, String> attributes) {}
