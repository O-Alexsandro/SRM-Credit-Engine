package com.srm.credit.engine.settlement.dto;

import com.srm.credit.engine.receivable.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementResponse(
        Long id,
        Long receivableId,
        BigDecimal amount,
        Currency currency,
        BigDecimal fxRateUsed,
        LocalDateTime settledAt
) {
}