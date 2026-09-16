package com.srm.credit.engine.pricing.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyConversionServiceTest {

    private final CurrencyConversionService service =
            new CurrencyConversionService();

    @Test
    void deveConverterRealParaDolar() {
        BigDecimal amount = new BigDecimal("92859.94");
        BigDecimal exchangeRate = new BigDecimal("5.4321");

        BigDecimal result = service.convert(amount, exchangeRate);

        assertEquals(
                new BigDecimal("17094.67"),
                result
        );
    }
}