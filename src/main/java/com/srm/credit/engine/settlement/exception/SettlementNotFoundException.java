package com.srm.credit.engine.settlement.exception;

public class SettlementNotFoundException extends RuntimeException {

    public SettlementNotFoundException(String message) {
        super(message);
    }
}