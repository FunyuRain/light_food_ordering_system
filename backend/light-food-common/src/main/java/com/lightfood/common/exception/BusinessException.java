package com.lightfood.common.exception;

/**
 * Business rule violation that should be shown to the caller.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
