package com.srm.credit.engine.settlement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srm.credit.engine.receivable.enums.Currency;
import com.srm.credit.engine.receivable.enums.ReceivableType;
import com.srm.credit.engine.settlement.dto.SettlementRequest;
import com.srm.credit.engine.settlement.dto.SettlementResponse;
import com.srm.credit.engine.settlement.service.SettlementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SettlementController.class)
class SettlementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    @MockitoBean
    private SettlementService settlementService;

    @Test
    void deveCriarSettlementComSucesso() throws Exception {

        SettlementRequest request = new SettlementRequest(
                1L,
                Currency.BRL
        );

        SettlementResponse response = new SettlementResponse(
                1L,
                1L,
                new BigDecimal("92859.94"),
                Currency.BRL,
                null,
                LocalDateTime.of(2026, 9, 16, 10, 0)
        );

        when(settlementService.settle(any(SettlementRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.receivableId").value(1))
                .andExpect(jsonPath("$.amount").value(92859.94))
                .andExpect(jsonPath("$.currency").value("BRL"));
    }

    @Test
    void deveRetornar400QuandoReceivableIdNaoForInformado()
            throws Exception {

        String request = """
                {
                    "currency": "BRL"
                }
                """;

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400QuandoCurrencyNaoForInformada()
            throws Exception {

        String request = """
                {
                    "receivableId": 1
                }
                """;

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveListarSettlements() throws Exception {

        SettlementResponse settlement = new SettlementResponse(
                1L,
                1L,
                new BigDecimal("92859.94"),
                Currency.BRL,
                null,
                LocalDateTime.of(2026, 9, 16, 10, 0)
        );

        when(settlementService.findAll(
                null,
                null,
                null,
                null
        )).thenReturn(List.of(settlement));

        mockMvc.perform(get("/settlements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].receivableId").value(1))
                .andExpect(jsonPath("$[0].amount").value(92859.94))
                .andExpect(jsonPath("$[0].currency").value("BRL"));
    }

    @Test
    void deveListarSettlementsComFiltros() throws Exception {

        SettlementResponse settlement = new SettlementResponse(
                1L,
                1L,
                new BigDecimal("92859.94"),
                Currency.BRL,
                null,
                LocalDateTime.of(2026, 9, 16, 10, 0)
        );

        LocalDateTime from = LocalDateTime.of(
                2026, 9, 16, 0, 0
        );

        LocalDateTime to = LocalDateTime.of(
                2026, 9, 16, 23, 59, 59
        );

        when(settlementService.findAll(
                Currency.BRL,
                1L,
                from,
                to
        )).thenReturn(List.of(settlement));

        mockMvc.perform(get("/settlements")
                        .param("currency", "BRL")
                        .param("cedenteId", "1")
                        .param("from", "2026-09-16T00:00:00")
                        .param("to", "2026-09-16T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].currency").value("BRL"));
    }

    @Test
    void deveBuscarSettlementPorId() throws Exception {

        SettlementResponse settlement = new SettlementResponse(
                1L,
                1L,
                new BigDecimal("92859.94"),
                Currency.BRL,
                null,
                LocalDateTime.of(2026, 9, 16, 10, 0)
        );

        when(settlementService.findById(1L))
                .thenReturn(settlement);

        mockMvc.perform(get("/settlements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.receivableId").value(1))
                .andExpect(jsonPath("$.amount").value(92859.94))
                .andExpect(jsonPath("$.currency").value("BRL"));
    }
}