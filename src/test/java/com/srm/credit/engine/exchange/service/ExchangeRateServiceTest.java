package com.srm.credit.engine.exchange.service;

import com.srm.credit.engine.exchange.dto.ExchangeRateRequest;
import com.srm.credit.engine.exchange.dto.ExchangeRateResponse;
import com.srm.credit.engine.exchange.entity.ExchangeRate;
import com.srm.credit.engine.exchange.repository.ExchangeRateRepository;
import com.srm.credit.engine.receivable.enums.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    void deveCriarCotacao() {
        ExchangeRateRequest request = new ExchangeRateRequest(
                Currency.USD,
                Currency.BRL,
                new BigDecimal("5.4321"),
                LocalDateTime.of(2026, 9, 14, 16, 0)
        );

        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setId(1L);
        exchangeRate.setFromCurrency(Currency.USD);
        exchangeRate.setToCurrency(Currency.BRL);
        exchangeRate.setRate(new BigDecimal("5.4321"));
        exchangeRate.setEffectiveAt(request.effectiveAt());

        when(exchangeRateRepository.save(any(ExchangeRate.class)))
                .thenReturn(exchangeRate);

        ExchangeRateResponse response = exchangeRateService.criar(request);

        assertEquals(1L, response.id());
        assertEquals(Currency.USD, response.fromCurrency());
        assertEquals(Currency.BRL, response.toCurrency());
        assertEquals(new BigDecimal("5.4321"), response.rate());
        assertEquals(request.effectiveAt(), response.effectiveAt());

        verify(exchangeRateRepository).save(any(ExchangeRate.class));
    }

    @Test
    void deveListarCotacoes() {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setId(1L);
        exchangeRate.setFromCurrency(Currency.USD);
        exchangeRate.setToCurrency(Currency.BRL);
        exchangeRate.setRate(new BigDecimal("5.4321"));
        exchangeRate.setEffectiveAt(LocalDateTime.of(2026, 9, 14, 16, 0));

        when(exchangeRateRepository.findAll())
                .thenReturn(List.of(exchangeRate));

        List<ExchangeRateResponse> response = exchangeRateService.listar();

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).id());
        assertEquals(Currency.USD, response.get(0).fromCurrency());

        verify(exchangeRateRepository).findAll();
    }

    @Test
    void deveBuscarCotacaoPorId() {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setId(1L);
        exchangeRate.setFromCurrency(Currency.USD);
        exchangeRate.setToCurrency(Currency.BRL);
        exchangeRate.setRate(new BigDecimal("5.4321"));
        exchangeRate.setEffectiveAt(LocalDateTime.of(2026, 9, 14, 16, 0));

        when(exchangeRateRepository.findById(1L))
                .thenReturn(Optional.of(exchangeRate));

        ExchangeRateResponse response = exchangeRateService.buscarPorId(1L);

        assertEquals(1L, response.id());
        assertEquals(Currency.USD, response.fromCurrency());
        assertEquals(Currency.BRL, response.toCurrency());
        assertEquals(new BigDecimal("5.4321"), response.rate());

        verify(exchangeRateRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoQuandoCotacaoNaoForEncontrada() {
        when(exchangeRateRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> exchangeRateService.buscarPorId(1L)
        );

        assertEquals("Cotação não encontrada", exception.getMessage());

        verify(exchangeRateRepository).findById(1L);
    }
}