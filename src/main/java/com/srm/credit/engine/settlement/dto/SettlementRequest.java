package com.srm.credit.engine.settlement.dto;

import com.srm.credit.engine.receivable.enums.Currency;
import jakarta.validation.constraints.NotNull;

public record SettlementRequest(

        @NotNull(message = "ID do recebível é obrigatório")
        Long receivableId,

        @NotNull(message = "Moeda da liquidação é obrigatória")
        Currency currency
) {
}