package com.srm.credit.engine.pricing.service;

import com.srm.credit.engine.pricing.dto.PricingRequest;
import com.srm.credit.engine.pricing.dto.PricingResponse;
import com.srm.credit.engine.pricing.strategy.ChequePricingStrategy;
import com.srm.credit.engine.pricing.strategy.DuplicataPricingStrategy;
import com.srm.credit.engine.receivable.enums.Currency;
import com.srm.credit.engine.receivable.enums.ReceivableType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {

    private final PricingService service = new PricingService(
            new DuplicataPricingStrategy(),
            new ChequePricingStrategy(),
            new CurrencyConversionService()
    );

    @Test
    void deveCalcularDuplicataEmReais() {

        PricingRequest request = new PricingRequest(
                ReceivableType.DUPLICATA_MERCANTIL,
                new BigDecimal("100000.00"),
                3,
                Currency.BRL,
                null
        );

        PricingResponse response = service.calculate(request);

        assertEquals(
                new BigDecimal("92859.94"),
                response.presentValue()
        );
    }

    @Test
    void deveCalcularChequeEmReais() {

        PricingRequest request = new PricingRequest(
                ReceivableType.CHEQUE_PRE_DATADO,
                new BigDecimal("25000.00"),
                2,
                Currency.BRL,
                null
        );

        PricingResponse response = service.calculate(request);

        assertEquals(
                new BigDecimal("23337.77"),
                response.presentValue()
        );
    }

    @Test
    void deveCalcularDuplicataEmDolar() {

        PricingRequest request = new PricingRequest(
                ReceivableType.DUPLICATA_MERCANTIL,
                new BigDecimal("100000.00"),
                3,
                Currency.USD,
                new BigDecimal("5.4321")
        );

        PricingResponse response = service.calculate(request);

        assertEquals(
                new BigDecimal("17094.67"),
                response.presentValue()
        );

        assertEquals(
                Currency.USD,
                response.paymentCurrency()
        );
    }

    @Test
    void deveRetornarResultadoComDuasCasasDecimais() {

        PricingRequest request = new PricingRequest(
                ReceivableType.DUPLICATA_MERCANTIL,
                new BigDecimal("100000.00"),
                3,
                Currency.BRL,
                null
        );

        PricingResponse response = service.calculate(request);

        assertEquals(
                2,
                response.presentValue().scale()
        );
    }
}