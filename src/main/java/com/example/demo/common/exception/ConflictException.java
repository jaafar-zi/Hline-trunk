package com.example.demo.common.exception;


public class ConflictException extends BaseException {

    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }
}
