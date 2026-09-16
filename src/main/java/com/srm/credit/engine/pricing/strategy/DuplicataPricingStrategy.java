package com.srm.credit.engine.pricing.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Component
public class DuplicataPricingStrategy implements PricingStrategy {

    private static final BigDecimal BASE_RATE = new BigDecimal("0.01");
    private static final BigDecimal SPREAD = new BigDecimal("0.015");

    @Override
    public BigDecimal calculate(BigDecimal faceValue, Integer term) {

        BigDecimal rate = BigDecimal.ONE
                .add(BASE_RATE)
                .add(SPREAD);

        BigDecimal factor = rate.pow(term);

        return faceValue.divide(
                factor,
                new MathContext(20, RoundingMode.HALF_EVEN)
        );
    }
}