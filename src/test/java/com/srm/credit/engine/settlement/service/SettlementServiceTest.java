package com.srm.credit.engine.settlement.service;

import com.srm.credit.engine.exchange.entity.ExchangeRate;
import com.srm.credit.engine.exchange.repository.ExchangeRateRepository;
import com.srm.credit.engine.pricing.dto.PricingResponse;
import com.srm.credit.engine.pricing.service.PricingService;
import com.srm.credit.engine.receivable.entity.Receivable;
import com.srm.credit.engine.receivable.enums.Currency;
import com.srm.credit.engine.receivable.enums.ReceivableStatus;
import com.srm.credit.engine.receivable.enums.ReceivableType;
import com.srm.credit.engine.receivable.repository.ReceivableRepository;
import com.srm.credit.engine.settlement.dto.SettlementRequest;
import com.srm.credit.engine.settlement.dto.SettlementResponse;
import com.srm.credit.engine.settlement.entity.Settlement;
import com.srm.credit.engine.settlement.repository.SettlementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private ReceivableRepository receivableRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private PricingService pricingService;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private SettlementService service;

    @Test
    void deveLiquidarRecebivelEmBRL() {

        Receivable receivable = criarReceivable(
                1L,
                ReceivableStatus.AVAILABLE
        );

        PricingResponse pricingResponse = new PricingResponse(
                ReceivableType.DUPLICATA_MERCANTIL,
                new BigDecimal("100000.00"),
                3,
                Currency.BRL,
                new BigDecimal("92859.94"),
                new BigDecimal("7140.06")
        );

        Settlement savedSettlement = new Settlement();
        savedSettlement.setId(10L);
        savedSettlement.setReceivable(receivable);
        savedSettlement.setAmount(new BigDecimal("92859.94"));
        savedSettlement.setPresentValueBrl(new BigDecimal("92859.94"));
        savedSettlement.setCurrency(Currency.BRL);
        savedSettlement.setSettledAt(LocalDateTime.now());

        when(receivableRepository.findById(1L))
                .thenReturn(Optional.of(receivable));

        when(pricingService.calculate(any()))
                .thenReturn(pricingResponse);

        when(settlementRepository.save(any(Settlement.class)))
                .thenReturn(savedSettlement);

        SettlementRequest request = new SettlementRequest(
                1L,
                Currency.BRL
        );

        SettlementResponse response = service.settle(request);

        assertEquals(10L, response.id());
        assertEquals(1L, response.receivableId());
        assertEquals(new BigDecimal("100000.00"), response.faceValue());
        assertEquals(new BigDecimal("92859.94"), response.presentValueBrl());
        assertEquals(new BigDecimal("92859.94"), response.amount());
        assertEquals(new BigDecimal("7140.06"), response.discount());
        assertEquals(Currency.BRL, response.currency());

        ArgumentCaptor<Settlement> settlementCaptor =
                ArgumentCaptor.forClass(Settlement.class);

        verify(settlementRepository).save(settlementCaptor.capture());

        Settlement settlement = settlementCaptor.getValue();

        assertEquals(
                new BigDecimal("92859.94"),
                settlement.getPresentValueBrl()
        );

        verify(receivableRepository).save(receivable);

        assertEquals(
                ReceivableStatus.SETTLED,
                receivable.getStatus()
        );
    }

    @Test
    void deveRetornarErroQuandoRecebivelNaoExistir() {

        when(receivableRepository.findById(99L))
                .thenReturn(Optional.empty());

        SettlementRequest request = new SettlementRequest(
                99L,
                Currency.BRL
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.settle(request)
        );

        assertEquals(
                "Recebível não encontrado",
                exception.getMessage()
        );

        verify(settlementRepository, never()).save(any());
        verify(pricingService, never()).calculate(any());
    }

    @Test
    void deveRetornarLiquidacaoExistenteQuandoRecebivelJaEstiverLiquidado() {

        Receivable receivable = criarReceivable(
                1L,
                ReceivableStatus.SETTLED
        );

        Settlement existingSettlement = new Settlement();
        existingSettlement.setId(10L);
        existingSettlement.setReceivable(receivable);
        existingSettlement.setAmount(new BigDecimal("92859.94"));
        existingSettlement.setPresentValueBrl(new BigDecimal("92859.94"));
        existingSettlement.setCurrency(Currency.BRL);
        existingSettlement.setSettledAt(LocalDateTime.now());

        when(receivableRepository.findById(1L))
                .thenReturn(Optional.of(receivable));

        when(settlementRepository.findByReceivableId(1L))
                .thenReturn(Optional.of(existingSettlement));

        SettlementRequest request = new SettlementRequest(
                1L,
                Currency.BRL
        );

        SettlementResponse response = service.settle(request);

        assertEquals(10L, response.id());
        assertEquals(1L, response.receivableId());
        assertEquals(new BigDecimal("100000.00"), response.faceValue());
        assertEquals(new BigDecimal("92859.94"), response.presentValueBrl());
        assertEquals(new BigDecimal("92859.94"), response.amount());
        assertEquals(new BigDecimal("7140.06"), response.discount());
        assertEquals(Currency.BRL, response.currency());

        verify(settlementRepository, never()).save(any());
        verify(pricingService, never()).calculate(any());
        verify(receivableRepository, never()).save(any());
    }

    @Test
    void deveRetornarErroQuandoRecebivelLiquidadoNaoPossuirSettlement() {

        Receivable receivable = criarReceivable(
                1L,
                ReceivableStatus.SETTLED
        );

        when(receivableRepository.findById(1L))
                .thenReturn(Optional.of(receivable));

        when(settlementRepository.findByReceivableId(1L))
                .thenReturn(Optional.empty());

        SettlementRequest request = new SettlementRequest(
                1L,
                Currency.BRL
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.settle(request)
        );

        assertEquals(
                "Liquidação do recebível não encontrada",
                exception.getMessage()
        );

        verify(settlementRepository, never()).save(any());
        verify(pricingService, never()).calculate(any());
    }

    @Test
    void deveLiquidarRecebivelEmUSDUsandoCotacaoMaisRecente() {

        Receivable receivable = criarReceivable(
                2L,
                ReceivableStatus.AVAILABLE
        );

        ExchangeRate olderRate = new ExchangeRate();
        olderRate.setId(1L);
        olderRate.setFromCurrency(Currency.USD);
        olderRate.setToCurrency(Currency.BRL);
        olderRate.setRate(new BigDecimal("5.20"));
        olderRate.setEffectiveAt(
                LocalDateTime.of(2026, 9, 13, 10, 0)
        );

        ExchangeRate latestRate = new ExchangeRate();
        latestRate.setId(2L);
        latestRate.setFromCurrency(Currency.USD);
        latestRate.setToCurrency(Currency.BRL);
        latestRate.setRate(new BigDecimal("5.4321"));
        latestRate.setEffectiveAt(
                LocalDateTime.of(2026, 9, 14, 16, 0)
        );

        PricingResponse brlPricingResponse = new PricingResponse(
                ReceivableType.DUPLICATA_MERCANTIL,
                new BigDecimal("100000.00"),
                3,
                Currency.BRL,
                new BigDecimal("92859.94"),
                new BigDecimal("7140.06")
        );

        PricingResponse usdPricingResponse = new PricingResponse(
                ReceivableType.DUPLICATA_MERCANTIL,
                new BigDecimal("100000.00"),
                3,
                Currency.USD,
                new BigDecimal("17094.67"),
                new BigDecimal("7140.06")
        );

        Settlement savedSettlement = new Settlement();
        savedSettlement.setId(20L);
        savedSettlement.setReceivable(receivable);
        savedSettlement.setAmount(new BigDecimal("17094.67"));
        savedSettlement.setPresentValueBrl(new BigDecimal("92859.94"));
        savedSettlement.setCurrency(Currency.USD);
        savedSettlement.setFxRateUsed(new BigDecimal("5.4321"));
        savedSettlement.setSettledAt(LocalDateTime.now());

        when(receivableRepository.findById(2L))
                .thenReturn(Optional.of(receivable));

        when(exchangeRateRepository.findAll())
                .thenReturn(List.of(olderRate, latestRate));

        when(pricingService.calculate(any()))
                .thenReturn(brlPricingResponse)
                .thenReturn(usdPricingResponse);

        when(settlementRepository.save(any(Settlement.class)))
                .thenReturn(savedSettlement);

        SettlementRequest request = new SettlementRequest(
                2L,
                Currency.USD
        );

        SettlementResponse response = service.settle(request);

        assertEquals(20L, response.id());
        assertEquals(new BigDecimal("100000.00"), response.faceValue());
        assertEquals(new BigDecimal("92859.94"), response.presentValueBrl());
        assertEquals(new BigDecimal("17094.67"), response.amount());
        assertEquals(new BigDecimal("7140.06"), response.discount());
        assertEquals(Currency.USD, response.currency());
        assertEquals(
                new BigDecimal("5.4321"),
                response.fxRateUsed()
        );

        ArgumentCaptor<Settlement> settlementCaptor =
                ArgumentCaptor.forClass(Settlement.class);

        verify(settlementRepository).save(settlementCaptor.capture());

        Settlement settlement = settlementCaptor.getValue();

        assertEquals(
                new BigDecimal("92859.94"),
                settlement.getPresentValueBrl()
        );

        verify(receivableRepository).save(receivable);
    }

    @Test
    void deveBuscarSettlementPorId() {

        Receivable receivable = criarReceivable(
                1L,
                ReceivableStatus.SETTLED
        );

        Settlement settlement = new Settlement();
        settlement.setId(10L);
        settlement.setReceivable(receivable);
        settlement.setAmount(new BigDecimal("92859.94"));
        settlement.setPresentValueBrl(new BigDecimal("92859.94"));
        settlement.setCurrency(Currency.BRL);
        settlement.setSettledAt(LocalDateTime.now());

        when(settlementRepository.findById(10L))
                .thenReturn(Optional.of(settlement));

        SettlementResponse response = service.findById(10L);

        assertEquals(10L, response.id());
        assertEquals(1L, response.receivableId());
        assertEquals(new BigDecimal("100000.00"), response.faceValue());
        assertEquals(new BigDecimal("92859.94"), response.presentValueBrl());
        assertEquals(new BigDecimal("92859.94"), response.amount());
        assertEquals(new BigDecimal("7140.06"), response.discount());
        assertEquals(Currency.BRL, response.currency());
    }

    @Test
    void deveRetornarErroQuandoSettlementNaoExistir() {

        when(settlementRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.findById(999L)
        );

        assertEquals(
                "Liquidação não encontrada",
                exception.getMessage()
        );
    }

    @Test
    void deveListarSettlementsSemFiltros() {

        Receivable receivable = criarReceivable(
                1L,
                ReceivableStatus.SETTLED
        );

        Settlement settlement = new Settlement();
        settlement.setId(10L);
        settlement.setReceivable(receivable);
        settlement.setAmount(new BigDecimal("92859.94"));
        settlement.setPresentValueBrl(new BigDecimal("92859.94"));
        settlement.setCurrency(Currency.BRL);
        settlement.setSettledAt(
                LocalDateTime.of(2026, 9, 16, 10, 0)
        );

        when(settlementRepository.findAll())
                .thenReturn(List.of(settlement));

        List<SettlementResponse> response = service.findAll(
                null,
                null,
                null,
                null
        );

        assertEquals(1, response.size());
        assertEquals(10L, response.get(0).id());
        assertEquals(Currency.BRL, response.get(0).currency());

        verify(settlementRepository).findAll();
    }

    @Test
    void deveFiltrarSettlementsPorMoeda() {

        Receivable receivable = criarReceivable(
                1L,
                ReceivableStatus.SETTLED
        );

        Settlement brlSettlement = new Settlement();
        brlSettlement.setId(10L);
        brlSettlement.setReceivable(receivable);
        brlSettlement.setAmount(new BigDecimal("92859.94"));
        brlSettlement.setPresentValueBrl(new BigDecimal("92859.94"));
        brlSettlement.setCurrency(Currency.BRL);
        brlSettlement.setSettledAt(
                LocalDateTime.of(2026, 9, 16, 10, 0)
        );

        Settlement usdSettlement = new Settlement();
        usdSettlement.setId(20L);
        usdSettlement.setReceivable(receivable);
        usdSettlement.setAmount(new BigDecimal("17094.67"));
        usdSettlement.setPresentValueBrl(new BigDecimal("92859.94"));
        usdSettlement.setCurrency(Currency.USD);
        usdSettlement.setSettledAt(
                LocalDateTime.of(2026, 9, 16, 11, 0)
        );

        when(settlementRepository.findAll())
                .thenReturn(List.of(brlSettlement, usdSettlement));

        List<SettlementResponse> response = service.findAll(
                Currency.BRL,
                null,
                null,
                null
        );

        assertEquals(1, response.size());
        assertEquals(10L, response.get(0).id());
        assertEquals(Currency.BRL, response.get(0).currency());
    }

    @Test
    void deveFiltrarSettlementsPorCedente() {

        Receivable receivableCedente1 = criarReceivable(
                1L,
                ReceivableStatus.SETTLED
        );

        Receivable receivableCedente2 = criarReceivable(
                2L,
                ReceivableStatus.SETTLED
        );

        com.srm.credit.engine.cedente.entity.Cedente cedente1 =
                new com.srm.credit.engine.cedente.entity.Cedente();
        cedente1.setId(1L);

        com.srm.credit.engine.cedente.entity.Cedente cedente2 =
                new com.srm.credit.engine.cedente.entity.Cedente();
        cedente2.setId(2L);

        receivableCedente1.setCedente(cedente1);
        receivableCedente2.setCedente(cedente2);

        Settlement settlement1 = new Settlement();
        settlement1.setId(10L);
        settlement1.setReceivable(receivableCedente1);
        settlement1.setAmount(new BigDecimal("92859.94"));
        settlement1.setPresentValueBrl(new BigDecimal("92859.94"));
        settlement1.setCurrency(Currency.BRL);
        settlement1.setSettledAt(
                LocalDateTime.of(2026, 9, 16, 10, 0)
        );

        Settlement settlement2 = new Settlement();
        settlement2.setId(20L);
        settlement2.setReceivable(receivableCedente2);
        settlement2.setAmount(new BigDecimal("50000.00"));
        settlement2.setPresentValueBrl(new BigDecimal("50000.00"));
        settlement2.setCurrency(Currency.BRL);
        settlement2.setSettledAt(
                LocalDateTime.of(2026, 9, 16, 11, 0)
        );

        when(settlementRepository.findAll())
                .thenReturn(List.of(settlement1, settlement2));

        List<SettlementResponse> response = service.findAll(
                null,
                1L,
                null,
                null
        );

        assertEquals(1, response.size());
        assertEquals(10L, response.get(0).id());
        assertEquals(1L, response.get(0).receivableId());
    }

    @Test
    void deveFiltrarSettlementsPorPeriodo() {

        Receivable receivable = criarReceivable(
                1L,
                ReceivableStatus.SETTLED
        );

        Settlement settlementDentroPeriodo = new Settlement();
        settlementDentroPeriodo.setId(10L);
        settlementDentroPeriodo.setReceivable(receivable);
        settlementDentroPeriodo.setAmount(new BigDecimal("92859.94"));
        settlementDentroPeriodo.setPresentValueBrl(new BigDecimal("92859.94"));
        settlementDentroPeriodo.setCurrency(Currency.BRL);
        settlementDentroPeriodo.setSettledAt(
                LocalDateTime.of(2026, 9, 16, 10, 0)
        );

        Settlement settlementForaPeriodo = new Settlement();
        settlementForaPeriodo.setId(20L);
        settlementForaPeriodo.setReceivable(receivable);
        settlementForaPeriodo.setAmount(new BigDecimal("50000.00"));
        settlementForaPeriodo.setPresentValueBrl(new BigDecimal("50000.00"));
        settlementForaPeriodo.setCurrency(Currency.BRL);
        settlementForaPeriodo.setSettledAt(
                LocalDateTime.of(2026, 9, 15, 10, 0)
        );

        when(settlementRepository.findAll())
                .thenReturn(List.of(
                        settlementDentroPeriodo,
                        settlementForaPeriodo
                ));

        LocalDateTime from = LocalDateTime.of(
                2026, 9, 16, 0, 0
        );

        LocalDateTime to = LocalDateTime.of(
                2026, 9, 16, 23, 59, 59
        );

        List<SettlementResponse> response = service.findAll(
                null,
                null,
                from,
                to
        );

        assertEquals(1, response.size());
        assertEquals(10L, response.get(0).id());
    }

    @Test
    void deveCombinarFiltrosDeMoedaCedenteEPeriodo() {

        Receivable receivableCedente1 = criarReceivable(
                1L,
                ReceivableStatus.SETTLED
        );

        Receivable receivableCedente2 = criarReceivable(
                2L,
                ReceivableStatus.SETTLED
        );

        com.srm.credit.engine.cedente.entity.Cedente cedente1 =
                new com.srm.credit.engine.cedente.entity.Cedente();
        cedente1.setId(1L);

        com.srm.credit.engine.cedente.entity.Cedente cedente2 =
                new com.srm.credit.engine.cedente.entity.Cedente();
        cedente2.setId(2L);

        receivableCedente1.setCedente(cedente1);
        receivableCedente2.setCedente(cedente2);

        Settlement settlementValido = new Settlement();
        settlementValido.setId(10L);
        settlementValido.setReceivable(receivableCedente1);
        settlementValido.setAmount(new BigDecimal("92859.94"));
        settlementValido.setPresentValueBrl(new BigDecimal("92859.94"));
        settlementValido.setCurrency(Currency.BRL);
        settlementValido.setSettledAt(
                LocalDateTime.of(2026, 9, 16, 10, 0)
        );

        Settlement settlementMoedaErrada = new Settlement();
        settlementMoedaErrada.setId(20L);
        settlementMoedaErrada.setReceivable(receivableCedente1);
        settlementMoedaErrada.setAmount(new BigDecimal("17094.67"));
        settlementMoedaErrada.setPresentValueBrl(new BigDecimal("92859.94"));
        settlementMoedaErrada.setCurrency(Currency.USD);
        settlementMoedaErrada.setSettledAt(
                LocalDateTime.of(2026, 9, 16, 10, 0)
        );

        Settlement settlementCedenteErrado = new Settlement();
        settlementCedenteErrado.setId(30L);
        settlementCedenteErrado.setReceivable(receivableCedente2);
        settlementCedenteErrado.setAmount(new BigDecimal("92859.94"));
        settlementCedenteErrado.setPresentValueBrl(new BigDecimal("92859.94"));
        settlementCedenteErrado.setCurrency(Currency.BRL);
        settlementCedenteErrado.setSettledAt(
                LocalDateTime.of(2026, 9, 16, 10, 0)
        );

        Settlement settlementForaPeriodo = new Settlement();
        settlementForaPeriodo.setId(40L);
        settlementForaPeriodo.setReceivable(receivableCedente1);
        settlementForaPeriodo.setAmount(new BigDecimal("92859.94"));
        settlementForaPeriodo.setPresentValueBrl(new BigDecimal("92859.94"));
        settlementForaPeriodo.setCurrency(Currency.BRL);
        settlementForaPeriodo.setSettledAt(
                LocalDateTime.of(2026, 9, 15, 10, 0)
        );

        when(settlementRepository.findAll())
                .thenReturn(List.of(
                        settlementValido,
                        settlementMoedaErrada,
                        settlementCedenteErrado,
                        settlementForaPeriodo
                ));

        LocalDateTime from = LocalDateTime.of(
                2026, 9, 16, 0, 0
        );

        LocalDateTime to = LocalDateTime.of(
                2026, 9, 16, 23, 59, 59
        );

        List<SettlementResponse> response = service.findAll(
                Currency.BRL,
                1L,
                from,
                to
        );

        assertEquals(1, response.size());
        assertEquals(10L, response.get(0).id());
    }

    private Receivable criarReceivable(
            Long id,
            ReceivableStatus status
    ) {
        Receivable receivable = new Receivable();
        receivable.setId(id);
        receivable.setType(ReceivableType.DUPLICATA_MERCANTIL);
        receivable.setFaceValue(new BigDecimal("100000.00"));
        receivable.setTerm(3);
        receivable.setPaymentCurrency(Currency.BRL);
        receivable.setStatus(status);

        return receivable;
    }
}