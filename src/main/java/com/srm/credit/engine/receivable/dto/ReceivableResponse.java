package com.srm.credit.engine.receivable.dto;

import com.srm.credit.engine.receivable.enums.Currency;
import com.srm.credit.engine.receivable.enums.ReceivableStatus;
import com.srm.credit.engine.receivable.enums.ReceivableType;

import java.math.BigDecimal;

public record ReceivableResponse(

        Long id,
        Long cedenteId,
        ReceivableType type,
        BigDecimal faceValue,
        Integer term,
        Currency paymentCurrency,
        ReceivableStatus status
) {
}