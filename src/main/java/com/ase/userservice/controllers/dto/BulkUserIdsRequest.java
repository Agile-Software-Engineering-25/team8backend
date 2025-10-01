package com.ase.userservice.controllers.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BulkUserIdsRequest(@NotEmpty List<String> userIds) {}
