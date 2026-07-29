package com.nhnacademy.inventory.global.error;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.error(
                        e.getErrorCode().getCode(),
                        e.getErrorCode().getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e){
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse(GlobalErrorCode.INVALID_INPUT.getMessage());

        return ResponseEntity
                .status(GlobalErrorCode.INVALID_INPUT.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.INVALID_INPUT.getCode(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        return ResponseEntity
                .status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        GlobalErrorCode.INTERNAL_SERVER_ERROR.getMessage()
                ));
    }
}