package com.srm.credit.engine.pricing.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DuplicataPricingStrategyTest {

    private final DuplicataPricingStrategy strategy =
            new DuplicataPricingStrategy();

    @Test
    void deveCalcularValorPresenteDaDuplicata() {

        BigDecimal faceValue = new BigDecimal("100000.00");
        Integer term = 3;

        BigDecimal result = strategy.calculate(faceValue, term);

        assertEquals(
                new BigDecimal("92859.94"),
                result.setScale(2, RoundingMode.HALF_EVEN)
        );
    }
}