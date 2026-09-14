package com.srm.credit.engine.cedente.controller;

import com.srm.credit.engine.cedente.service.CedenteService;
import com.srm.credit.engine.cedente.dto.CedenteRequest;
import com.srm.credit.engine.cedente.dto.CedenteResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cedentes")
public class CedenteController {

    private final CedenteService cedenteService;

    public CedenteController(CedenteService cedenteService) {
        this.cedenteService = cedenteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CedenteResponse criar(@Valid @RequestBody CedenteRequest request) {
        return cedenteService.criar(request);
    }

    @GetMapping
    public List<CedenteResponse> listar() {
        return cedenteService.listar();
    }

    @GetMapping("/{id}")
    public CedenteResponse buscarPorId(@PathVariable Long id) {
        return cedenteService.buscarPorId(id);
    }
}