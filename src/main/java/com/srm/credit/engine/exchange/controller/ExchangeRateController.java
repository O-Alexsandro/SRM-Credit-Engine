package com.srm.credit.engine.exchange.controller;

import com.srm.credit.engine.exchange.dto.ExchangeRateRequest;
import com.srm.credit.engine.exchange.dto.ExchangeRateResponse;
import com.srm.credit.engine.exchange.service.ExchangeRateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExchangeRateResponse criar(@Valid @RequestBody ExchangeRateRequest request) {
        return exchangeRateService.criar(request);
    }

    @GetMapping
    public List<ExchangeRateResponse> listar() {
        return exchangeRateService.listar();
    }

    @GetMapping("/{id}")
    public ExchangeRateResponse buscarPorId(@PathVariable Long id) {
        return exchangeRateService.buscarPorId(id);
    }
}