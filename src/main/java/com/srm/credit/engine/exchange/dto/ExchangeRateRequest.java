package com.srm.credit.engine.exchange.dto;

import com.srm.credit.engine.receivable.enums.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExchangeRateRequest(

        @NotNull(message = "Moeda de origem é obrigatória")
        Currency fromCurrency,

        @NotNull(message = "Moeda de destino é obrigatória")
        Currency toCurrency,

        @NotNull(message = "Cotação é obrigatória")
        @Positive(message = "Cotação deve ser maior que zero")
        BigDecimal rate,

        @NotNull(message = "Data de vigência é obrigatória")
        LocalDateTime effectiveAt

) {}