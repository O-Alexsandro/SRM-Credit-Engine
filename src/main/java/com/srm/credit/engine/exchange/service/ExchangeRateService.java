package com.srm.credit.engine.exchange.service;

import com.srm.credit.engine.exchange.dto.ExchangeRateRequest;
import com.srm.credit.engine.exchange.dto.ExchangeRateResponse;
import com.srm.credit.engine.exchange.entity.ExchangeRate;
import com.srm.credit.engine.exchange.exception.ExchangeRateNotFoundException;
import com.srm.credit.engine.exchange.repository.ExchangeRateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public ExchangeRateResponse criar(ExchangeRateRequest request) {

        ExchangeRate exchangeRate = new ExchangeRate();

        exchangeRate.setFromCurrency(request.fromCurrency());
        exchangeRate.setToCurrency(request.toCurrency());
        exchangeRate.setRate(request.rate());
        exchangeRate.setEffectiveAt(request.effectiveAt());

        ExchangeRate exchangeRateSalvo = exchangeRateRepository.save(exchangeRate);

        return toResponse(exchangeRateSalvo);
    }

    public List<ExchangeRateResponse> listar() {
        return exchangeRateRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ExchangeRateResponse buscarPorId(Long id) {
        ExchangeRate exchangeRate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new ExchangeRateNotFoundException("Cotação não encontrada"));

        return toResponse(exchangeRate);
    }

    private ExchangeRateResponse toResponse(ExchangeRate exchangeRate) {
        return new ExchangeRateResponse(
                exchangeRate.getId(),
                exchangeRate.getFromCurrency(),
                exchangeRate.getToCurrency(),
                exchangeRate.getRate(),
                exchangeRate.getEffectiveAt()
        );
    }
}