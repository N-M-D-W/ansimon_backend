package com.nmdw.ansimon.global.error;

import org.springframework.web.reactive.function.client.WebClientException;

import java.util.Objects;

public class ExternalServiceException extends WebClientException {

    private final ErrorCode errorCode;

    public ExternalServiceException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public ExternalServiceException(ErrorCode errorCode, Throwable cause) {
        super(Objects.requireNonNull(errorCode, "errorCode must not be null").message(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
