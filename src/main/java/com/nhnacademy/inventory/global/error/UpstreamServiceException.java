package com.nhnacademy.inventory.global.error;

public class UpstreamServiceException extends BaseException {
    public UpstreamServiceException(Throwable cause) {
        super(GlobalErrorCode.UPSTREAM_SERVICE_ERROR);
        initCause(cause);
    }
}
