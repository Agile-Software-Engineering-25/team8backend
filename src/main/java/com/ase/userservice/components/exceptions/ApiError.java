package com.ase.userservice.components.exceptions;

import java.time.OffsetDateTime;

public record ApiError(String path, int status, String error, String message, OffsetDateTime timestamp) {}
