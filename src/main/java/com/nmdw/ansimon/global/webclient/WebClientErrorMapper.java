package com.nmdw.ansimon.global.webclient;

import com.nmdw.ansimon.global.error.ErrorCode;
import com.nmdw.ansimon.global.error.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class WebClientErrorMapper {

    private static final Logger log = LoggerFactory.getLogger(WebClientErrorMapper.class);

    public ExternalServiceException forStatus(HttpStatusCode statusCode) {
        return forStatus(statusCode, ExternalRequestContext.unknown());
    }

    public ExternalServiceException forStatus(HttpStatusCode statusCode, ExternalRequestContext context) {
        ErrorCode errorCode = statusCode.is4xxClientError()
                ? ErrorCode.EXTERNAL_SERVICE_CLIENT_ERROR
                : ErrorCode.EXTERNAL_SERVICE_SERVER_ERROR;
        log.warn("External service response failed: service={}, method={}, endpoint={}, status={}, correlationId={}",
                context.serviceId(), context.method(), context.endpoint(), statusCode.value(), context.correlationId());
        return new ExternalServiceException(errorCode);
    }

    public ExternalServiceException forFailure(Throwable failure) {
        return forFailure(failure, ExternalRequestContext.unknown());
    }

    public ExternalServiceException forFailure(Throwable failure, ExternalRequestContext context) {
        ErrorCode errorCode = hasTimeoutCause(failure)
                ? ErrorCode.EXTERNAL_SERVICE_TIMEOUT
                : ErrorCode.EXTERNAL_SERVICE_SERVER_ERROR;
        log.warn("External service request failed: service={}, method={}, endpoint={}, status={}, correlationId={}",
                context.serviceId(), context.method(), context.endpoint(), "unavailable", context.correlationId());
        return new ExternalServiceException(errorCode);
    }

    private boolean hasTimeoutCause(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof TimeoutException || current instanceof io.netty.handler.timeout.TimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    public record ExternalRequestContext(String serviceId, HttpMethod method, String endpoint, String correlationId) {

        public static ExternalRequestContext forService(String serviceId, HttpMethod method) {
            return new ExternalRequestContext(serviceId, method, "configured-service", UUID.randomUUID().toString());
        }

        private static ExternalRequestContext unknown() {
            return new ExternalRequestContext("unknown", null, "configured-service", "unavailable");
        }
    }
}