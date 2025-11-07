package com.ase.userservice.controllers.dto;

import java.util.List;

public record BulkUserIdsRequest(
    List<String> userIds
) {}
