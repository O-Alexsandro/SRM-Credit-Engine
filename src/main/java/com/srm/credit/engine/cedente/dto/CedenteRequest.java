package com.srm.credit.engine.cedente.dto;

import jakarta.validation.constraints.NotBlank;

public record CedenteRequest(

        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotBlank(message = "Documento é obrigatório")
        String document
) {
}