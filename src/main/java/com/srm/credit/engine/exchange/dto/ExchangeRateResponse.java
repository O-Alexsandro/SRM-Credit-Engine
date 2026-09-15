package com.srm.credit.engine.exchange.dto;

import com.srm.credit.engine.receivable.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExchangeRateResponse(
        Long id,
        Currency fromCurrency,
        Currency toCurrency,
        BigDecimal rate,
        LocalDateTime effectiveAt
) {}
