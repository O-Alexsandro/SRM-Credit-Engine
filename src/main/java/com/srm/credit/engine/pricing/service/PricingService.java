package com.srm.credit.engine.pricing.service;

import com.srm.credit.engine.pricing.dto.PricingRequest;
import com.srm.credit.engine.pricing.dto.PricingResponse;
import com.srm.credit.engine.pricing.strategy.ChequePricingStrategy;
import com.srm.credit.engine.pricing.strategy.DuplicataPricingStrategy;
import com.srm.credit.engine.pricing.strategy.PricingStrategy;
import com.srm.credit.engine.receivable.enums.Currency;
import com.srm.credit.engine.receivable.enums.ReceivableType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PricingService {

    private final DuplicataPricingStrategy duplicataPricingStrategy;
    private final ChequePricingStrategy chequePricingStrategy;
    private final CurrencyConversionService currencyConversionService;

    public PricingService(
            DuplicataPricingStrategy duplicataPricingStrategy,
            ChequePricingStrategy chequePricingStrategy,
            CurrencyConversionService currencyConversionService) {

        this.duplicataPricingStrategy = duplicataPricingStrategy;
        this.chequePricingStrategy = chequePricingStrategy;
        this.currencyConversionService = currencyConversionService;
    }

    public PricingResponse calculate(PricingRequest request) {

        PricingStrategy strategy = getStrategy(request.type());

        BigDecimal presentValueBrl = strategy.calculate(
                request.faceValue(),
                request.term()
        );

        // Arredonda o valor presente em BRL antes da conversão
        presentValueBrl = presentValueBrl.setScale(
                2,
                RoundingMode.HALF_EVEN
        );

        BigDecimal discount = request.faceValue()
                .setScale(2, RoundingMode.HALF_EVEN)
                .subtract(presentValueBrl)
                .setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal presentValue = presentValueBrl;

        if (request.paymentCurrency() == Currency.USD) {

            if (request.exchangeRate() == null ||
                    request.exchangeRate().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Cotação é obrigatória para pagamento em USD"
                );
            }

            presentValue = currencyConversionService.convert(
                    presentValueBrl,
                    request.exchangeRate()
            );
        }

        return new PricingResponse(
                request.type(),
                request.faceValue(),
                request.term(),
                request.paymentCurrency(),
                presentValue,
                discount
        );
    }

    private PricingStrategy getStrategy(ReceivableType type) {

        if (type == ReceivableType.DUPLICATA_MERCANTIL) {
            return duplicataPricingStrategy;
        }

        if (type == ReceivableType.CHEQUE_PRE_DATADO) {
            return chequePricingStrategy;
        }

        throw new IllegalArgumentException(
                "Tipo de recebível não suportado"
        );
    }
}