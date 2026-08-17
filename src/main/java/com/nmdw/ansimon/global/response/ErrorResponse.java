package com.nmdw.ansimon.global.response;

import java.util.Map;
import java.util.Objects;

public record ErrorResponse(
        String code,
        String message,
        Map<String, Object> details
) {

    public ErrorResponse {
        details = Map.copyOf(Objects.requireNonNull(details, "details must not be null"));
    }
}
