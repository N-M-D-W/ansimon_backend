package com.nmdw.ansimon.global.response;

public record ErrorResponse(
        String code,
        String message
) {
}