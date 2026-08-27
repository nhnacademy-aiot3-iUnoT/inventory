package com.nhnacademy.inventory.global.error;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.inventories.error.InventoryErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();

        if (errorCode.getStatus().is5xxServerError()) {
            log.error("BaseException(5xx): code={}", errorCode.getCode(), e);
        } else {
            log.warn("BaseException: status={}, code={}, message={}", errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage());
        }

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(
                        errorCode.getCode(),
                        errorCode.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e){
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse(GlobalErrorCode.INVALID_INPUT.getMessage());

        log.warn("MethodArgumentNotValidException: {}", message);

        return ResponseEntity
                .status(GlobalErrorCode.INVALID_INPUT.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.INVALID_INPUT.getCode(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {

        log.error("처리되지 않은 예외", e);

        return ResponseEntity
                .status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        GlobalErrorCode.INTERNAL_SERVER_ERROR.getMessage()
                ));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(ObjectOptimisticLockingFailureException e){

        log.warn("낙관적 락 충돌", e);

        return ResponseEntity.status(InventoryErrorCode.ENVIRONMENT_STANDARD_CONFLICT.getStatus())
                .body(ApiResponse.error(InventoryErrorCode.ENVIRONMENT_STANDARD_CONFLICT.getCode(),
                        InventoryErrorCode.ENVIRONMENT_STANDARD_CONFLICT.getMessage()
                        ));
    }




}
