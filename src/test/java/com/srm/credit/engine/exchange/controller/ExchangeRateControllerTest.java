package com.srm.credit.engine.exchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srm.credit.engine.exchange.dto.ExchangeRateResponse;
import com.srm.credit.engine.exchange.service.ExchangeRateService;
import com.srm.credit.engine.receivable.enums.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExchangeRateController.class)
class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExchangeRateService exchangeRateService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void deveCriarCotacao() throws Exception {
        ExchangeRateResponse response = new ExchangeRateResponse(
                1L,
                Currency.USD,
                Currency.BRL,
                new BigDecimal("5.4321"),
                LocalDateTime.of(2026, 9, 14, 16, 0)
        );

        when(exchangeRateService.criar(
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(response);

        String request = """
                {
                  "fromCurrency": "USD",
                  "toCurrency": "BRL",
                  "rate": 5.4321,
                  "effectiveAt": "2026-09-14T16:00:00"
                }
                """;

        mockMvc.perform(post("/exchange-rates")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("BRL"))
                .andExpect(jsonPath("$.rate").value(5.4321));
    }

    @Test
    void deveListarCotacoes() throws Exception {
        ExchangeRateResponse response = new ExchangeRateResponse(
                1L,
                Currency.USD,
                Currency.BRL,
                new BigDecimal("5.4321"),
                LocalDateTime.of(2026, 9, 14, 16, 0)
        );

        when(exchangeRateService.listar())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fromCurrency").value("USD"));
    }

    @Test
    void deveBuscarCotacaoPorId() throws Exception {
        ExchangeRateResponse response = new ExchangeRateResponse(
                1L,
                Currency.USD,
                Currency.BRL,
                new BigDecimal("5.4321"),
                LocalDateTime.of(2026, 9, 14, 16, 0)
        );

        when(exchangeRateService.buscarPorId(anyLong()))
                .thenReturn(response);

        mockMvc.perform(get("/exchange-rates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rate").value(5.4321));
    }

    @Test
    void deveRetornar400QuandoDadosForemInvalidos() throws Exception {
        String request = """
                {
                  "fromCurrency": "USD",
                  "toCurrency": "BRL",
                  "rate": -1,
                  "effectiveAt": "2026-09-14T16:00:00"
                }
                """;

        mockMvc.perform(post("/exchange-rates")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest());
    }
}