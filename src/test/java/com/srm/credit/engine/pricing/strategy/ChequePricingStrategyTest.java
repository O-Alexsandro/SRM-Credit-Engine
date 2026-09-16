package com.srm.credit.engine.pricing.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChequePricingStrategyTest {

    private final ChequePricingStrategy strategy =
            new ChequePricingStrategy();

    @Test
    void deveCalcularValorPresenteDoCheque() {

        BigDecimal faceValue = new BigDecimal("25000.00");
        Integer term = 2;

        BigDecimal result = strategy.calculate(faceValue, term);

        assertEquals(
                new BigDecimal("23337.77"),
                result.setScale(2, RoundingMode.HALF_EVEN)
        );
    }
}