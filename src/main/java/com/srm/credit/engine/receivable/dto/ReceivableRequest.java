package com.srm.credit.engine.receivable.dto;

import com.srm.credit.engine.receivable.enums.Currency;
import com.srm.credit.engine.receivable.enums.ReceivableType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ReceivableRequest(

        @NotNull(message = "Cedente é obrigatório")
        Long cedenteId,

        @NotNull(message = "Tipo do recebível é obrigatório")
        ReceivableType type,

        @NotNull(message = "Valor de face é obrigatório")
        @Positive(message = "Valor de face deve ser maior que zero")
        BigDecimal faceValue,

        @NotNull(message = "Prazo é obrigatório")
        @Positive(message = "Prazo deve ser maior que zero")
        Integer term,

        @NotNull(message = "Moeda é obrigatória")
        Currency paymentCurrency
) {
}