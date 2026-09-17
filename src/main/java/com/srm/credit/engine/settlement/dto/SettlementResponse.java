package com.srm.credit.engine.settlement.dto;

import com.srm.credit.engine.receivable.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementResponse(
        Long id,
        Long receivableId,
        BigDecimal faceValue,
        BigDecimal presentValueBrl,
        BigDecimal amount,
        BigDecimal discount,
        Currency currency,
        BigDecimal fxRateUsed,
        LocalDateTime settledAt
) {
}