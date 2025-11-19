package com.example.demo.common.exception;

public class BadRequestException extends BaseException {

    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
}
