package com.example.demo.common.exception;

import java.time.Instant;

public record HttpErrorResponse(
        Instant timestamp,
        String path,
        ErrorCode errorCode,
        String message
) {}
