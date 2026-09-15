package com.srm.credit.engine.receivable.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srm.credit.engine.receivable.dto.ReceivableRequest;
import com.srm.credit.engine.receivable.dto.ReceivableResponse;
import com.srm.credit.engine.receivable.enums.Currency;
import com.srm.credit.engine.receivable.enums.ReceivableStatus;
import com.srm.credit.engine.receivable.enums.ReceivableType;
import com.srm.credit.engine.receivable.service.ReceivableService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReceivableController.class)
class ReceivableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ReceivableService receivableService;

    @Test
    void deveCriarReceivable() throws Exception {

        ReceivableRequest request = new ReceivableRequest(
                1L,
                ReceivableType.DUPLICATA_MERCANTIL,
                new BigDecimal("100000.00"),
                3,
                Currency.BRL
        );

        ReceivableResponse response = new ReceivableResponse(
                1L,
                1L,
                ReceivableType.DUPLICATA_MERCANTIL,
                new BigDecimal("100000.00"),
                3,
                Currency.BRL,
                ReceivableStatus.AVAILABLE
        );

        when(receivableService.criar(any(ReceivableRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/receivables")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cedenteId").value(1))
                .andExpect(jsonPath("$.type")
                        .value("DUPLICATA_MERCANTIL"))
                .andExpect(jsonPath("$.faceValue").value(100000.00))
                .andExpect(jsonPath("$.term").value(3))
                .andExpect(jsonPath("$.paymentCurrency").value("BRL"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void deveListarReceivables() throws Exception {

        ReceivableResponse receivable1 = new ReceivableResponse(
                1L,
                1L,
                ReceivableType.DUPLICATA_MERCANTIL,
                new BigDecimal("100000.00"),
                3,
                Currency.BRL,
                ReceivableStatus.AVAILABLE
        );

        ReceivableResponse receivable2 = new ReceivableResponse(
                2L,
                1L,
                ReceivableType.CHEQUE_PRE_DATADO,
                new BigDecimal("25000.00"),
                2,
                Currency.USD,
                ReceivableStatus.AVAILABLE
        );

        when(receivableService.listar())
                .thenReturn(List.of(receivable1, receivable2));

        mockMvc.perform(
                        get("/receivables")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].cedenteId").value(1))
                .andExpect(jsonPath("$[0].type")
                        .value("DUPLICATA_MERCANTIL"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].type")
                        .value("CHEQUE_PRE_DATADO"));
    }

    @Test
    void deveBuscarReceivablePorId() throws Exception {

        ReceivableResponse response = new ReceivableResponse(
                1L,
                1L,
                ReceivableType.DUPLICATA_MERCANTIL,
                new BigDecimal("100000.00"),
                3,
                Currency.BRL,
                ReceivableStatus.AVAILABLE
        );

        when(receivableService.buscarPorId(1L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/receivables/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cedenteId").value(1))
                .andExpect(jsonPath("$.type")
                        .value("DUPLICATA_MERCANTIL"))
                .andExpect(jsonPath("$.faceValue").value(100000.00))
                .andExpect(jsonPath("$.term").value(3))
                .andExpect(jsonPath("$.paymentCurrency").value("BRL"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void deveRetornarBadRequestQuandoDadosForemInvalidos() throws Exception {

        ReceivableRequest request = new ReceivableRequest(
                null,
                null,
                new BigDecimal("-100.00"),
                0,
                null
        );

        mockMvc.perform(
                        post("/receivables")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }
}