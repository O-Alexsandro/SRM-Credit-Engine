package com.srm.credit.engine.receivable.exception;

public class ReceivableNotFoundException extends RuntimeException {

    public ReceivableNotFoundException(String message) {
        super(message);
    }
}