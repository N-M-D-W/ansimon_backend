package com.nmdw.ansimon.global.error;

import com.nmdw.ansimon.global.response.ApiResponse;
import com.nmdw.ansimon.global.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return failure(exception.errorCode(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        List<Map<String, String>> fields = exception.getBindingResult().getFieldErrors().stream()
                .map(this::safeFieldDetail)
                .toList();
        return failure(ErrorCode.VALIDATION_ERROR, Map.of("fields", fields));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return failure(ErrorCode.VALIDATION_ERROR, Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableRequest(HttpMessageNotReadableException exception) {
        return failure(ErrorCode.MALFORMED_REQUEST, Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingResource(NoResourceFoundException exception) {
        return failure(ErrorCode.RESOURCE_NOT_FOUND, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        return failure(ErrorCode.INTERNAL_ERROR, Map.of());
    }

    private Map<String, String> safeFieldDetail(FieldError fieldError) {
        return Map.of(
                "field", fieldError.getField(),
                "message", "Invalid value."
        );
    }

    private ResponseEntity<ApiResponse<Void>> failure(ErrorCode errorCode, Map<String, Object> details) {
        ErrorResponse error = new ErrorResponse(errorCode.code(), errorCode.message(), details);
        return ResponseEntity.status(errorCode.status()).body(ApiResponse.failure(error));
    }
}
