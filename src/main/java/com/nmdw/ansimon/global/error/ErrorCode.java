package com.nmdw.ansimon.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_ERROR("GLOBAL_VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "The request is invalid."),
    MALFORMED_REQUEST("GLOBAL_MALFORMED_REQUEST", HttpStatus.BAD_REQUEST, "The request could not be read."),
    UNSUPPORTED_MEDIA_TYPE("GLOBAL_UNSUPPORTED_MEDIA_TYPE", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "The request media type is not supported."),
    NOT_ACCEPTABLE("GLOBAL_NOT_ACCEPTABLE", HttpStatus.NOT_ACCEPTABLE, "The requested response media type is not supported."),
    METHOD_NOT_ALLOWED("GLOBAL_METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, "The request method is not supported."),
    RESOURCE_NOT_FOUND("GLOBAL_RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, "The requested resource was not found."),
    CONFLICT("GLOBAL_CONFLICT", HttpStatus.CONFLICT, "The request conflicts with the current state."),
    INTERNAL_ERROR("GLOBAL_INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred."),
    EXTERNAL_SERVICE_TIMEOUT("GLOBAL_EXTERNAL_SERVICE_TIMEOUT", HttpStatus.GATEWAY_TIMEOUT, "An external service timed out."),
    EXTERNAL_SERVICE_CLIENT_ERROR("GLOBAL_EXTERNAL_SERVICE_CLIENT_ERROR", HttpStatus.BAD_GATEWAY, "An external service rejected the request."),
    EXTERNAL_SERVICE_SERVER_ERROR("GLOBAL_EXTERNAL_SERVICE_SERVER_ERROR", HttpStatus.BAD_GATEWAY, "An external service is unavailable.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public String code() { return code; }
    public HttpStatus status() { return status; }
    public String message() { return message; }
}
