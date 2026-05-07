package com.example.foodshop.payment.exception;

public class VoucherException extends RuntimeException {
    
    public VoucherException(String message) {
        super(message);
    }
    
    public VoucherException(String message, Throwable cause) {
        super(message, cause);
    }
}
