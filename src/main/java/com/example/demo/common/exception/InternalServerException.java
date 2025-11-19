package com.example.demo.common.exception;

public class InternalServerException extends BaseException {

    public InternalServerException(String message) {
        super(ErrorCode.INTERNAL_ERROR, message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(ErrorCode.INTERNAL_ERROR, message, cause);
    }
}
