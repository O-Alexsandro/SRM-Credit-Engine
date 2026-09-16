package com.srm.credit.engine.pricing.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CurrencyConversionService {

    public BigDecimal convert(
            BigDecimal amount,
            BigDecimal exchangeRate
    ) {
        return amount
                .divide(exchangeRate, 10, RoundingMode.HALF_EVEN)
                .setScale(2, RoundingMode.HALF_EVEN);
    }
}