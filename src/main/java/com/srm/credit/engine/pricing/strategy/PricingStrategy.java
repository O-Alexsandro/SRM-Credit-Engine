package com.srm.credit.engine.pricing.strategy;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculate(BigDecimal faceValue, Integer term);
}