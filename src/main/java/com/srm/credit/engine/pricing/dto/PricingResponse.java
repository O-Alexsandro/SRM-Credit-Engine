package com.srm.credit.engine.pricing.dto;

import com.srm.credit.engine.receivable.enums.Currency;
import com.srm.credit.engine.receivable.enums.ReceivableType;

import java.math.BigDecimal;

public record PricingResponse(
        ReceivableType type,
        BigDecimal faceValue,
        Integer term,
        Currency paymentCurrency,
        BigDecimal presentValue,
        BigDecimal discount
) {}