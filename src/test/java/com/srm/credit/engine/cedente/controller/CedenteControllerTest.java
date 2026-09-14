package com.srm.credit.engine.cedente.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srm.credit.engine.cedente.dto.CedenteRequest;
import com.srm.credit.engine.cedente.dto.CedenteResponse;
import com.srm.credit.engine.cedente.service.CedenteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CedenteController.class)
class CedenteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CedenteService cedenteService;

    @Test
    void deveCriarCedente() throws Exception {

        CedenteRequest request = new CedenteRequest(
                "Empresa Teste",
                "12345678000199"
        );

        CedenteResponse response = new CedenteResponse(
                1L,
                "Empresa Teste",
                "12345678000199"
        );

        when(cedenteService.criar(any(CedenteRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/cedentes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Empresa Teste"))
                .andExpect(jsonPath("$.document").value("12345678000199"));
    }

    @Test
    void deveListarCedentes() throws Exception {

        CedenteResponse cedente1 = new CedenteResponse(
                1L,
                "Empresa 1",
                "11111111000111"
        );

        CedenteResponse cedente2 = new CedenteResponse(
                2L,
                "Empresa 2",
                "22222222000122"
        );

        when(cedenteService.listar())
                .thenReturn(List.of(cedente1, cedente2));

        mockMvc.perform(
                        get("/cedentes")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Empresa 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Empresa 2"));
    }

    @Test
    void deveBuscarCedentePorId() throws Exception {

        CedenteResponse response = new CedenteResponse(
                1L,
                "Empresa Teste",
                "12345678000199"
        );

        when(cedenteService.buscarPorId(1L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/cedentes/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Empresa Teste"))
                .andExpect(jsonPath("$.document").value("12345678000199"));
    }

    @Test
    void deveRetornarBadRequestQuandoDadosForemInvalidos() throws Exception {

        CedenteRequest request = new CedenteRequest(
                "",
                ""
        );

        mockMvc.perform(
                        post("/cedentes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }
}