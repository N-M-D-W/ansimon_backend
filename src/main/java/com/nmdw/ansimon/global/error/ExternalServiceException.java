package com.nmdw.ansimon.global.error;

public class ExternalServiceException extends BusinessException {

    public ExternalServiceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ExternalServiceException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
