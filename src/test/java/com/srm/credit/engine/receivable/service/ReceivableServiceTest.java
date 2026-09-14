package com.srm.credit.engine.receivable.service;

import com.srm.credit.engine.cedente.entity.Cedente;
import com.srm.credit.engine.cedente.repository.CedenteRepository;
import com.srm.credit.engine.receivable.dto.ReceivableRequest;
import com.srm.credit.engine.receivable.dto.ReceivableResponse;
import com.srm.credit.engine.receivable.entity.Receivable;
import com.srm.credit.engine.receivable.enums.Currency;
import com.srm.credit.engine.receivable.enums.ReceivableStatus;
import com.srm.credit.engine.receivable.enums.ReceivableType;
import com.srm.credit.engine.receivable.repository.ReceivableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceivableServiceTest {

    @Mock
    private ReceivableRepository receivableRepository;

    @Mock
    private CedenteRepository cedenteRepository;

    @InjectMocks
    private ReceivableService receivableService;

    @Test
    void deveCriarReceivable() {

        ReceivableRequest request = new ReceivableRequest(
                1L,
                ReceivableType.DUPLICATA_MERCANTIL,
                new BigDecimal("100000.00"),
                3,
                Currency.BRL
        );

        Cedente cedente = new Cedente();
        cedente.setId(1L);
        cedente.setName("Empresa Teste");
        cedente.setDocument("12345678000199");

        Receivable receivableSalvo = new Receivable();
        receivableSalvo.setId(1L);
        receivableSalvo.setCedente(cedente);
        receivableSalvo.setType(ReceivableType.DUPLICATA_MERCANTIL);
        receivableSalvo.setFaceValue(new BigDecimal("100000.00"));
        receivableSalvo.setTerm(3);
        receivableSalvo.setPaymentCurrency(Currency.BRL);
        receivableSalvo.setStatus(ReceivableStatus.AVAILABLE);

        when(cedenteRepository.findById(1L))
                .thenReturn(Optional.of(cedente));

        when(receivableRepository.save(any(Receivable.class)))
                .thenReturn(receivableSalvo);

        ReceivableResponse response = receivableService.criar(request);

        assertEquals(1L, response.id());
        assertEquals(1L, response.cedenteId());
        assertEquals(
                ReceivableType.DUPLICATA_MERCANTIL,
                response.type()
        );
        assertEquals(
                new BigDecimal("100000.00"),
                response.faceValue()
        );
        assertEquals(3, response.term());
        assertEquals(Currency.BRL, response.paymentCurrency());
        assertEquals(ReceivableStatus.AVAILABLE, response.status());

        verify(cedenteRepository).findById(1L);
        verify(receivableRepository).save(any(Receivable.class));
    }

    @Test
    void deveListarReceivables() {

        Cedente cedente = new Cedente();
        cedente.setId(1L);
        cedente.setName("Empresa Teste");
        cedente.setDocument("12345678000199");

        Receivable receivable1 = new Receivable();
        receivable1.setId(1L);
        receivable1.setCedente(cedente);
        receivable1.setType(ReceivableType.DUPLICATA_MERCANTIL);
        receivable1.setFaceValue(new BigDecimal("100000.00"));
        receivable1.setTerm(3);
        receivable1.setPaymentCurrency(Currency.BRL);
        receivable1.setStatus(ReceivableStatus.AVAILABLE);

        Receivable receivable2 = new Receivable();
        receivable2.setId(2L);
        receivable2.setCedente(cedente);
        receivable2.setType(ReceivableType.CHEQUE_PRE_DATADO);
        receivable2.setFaceValue(new BigDecimal("25000.00"));
        receivable2.setTerm(2);
        receivable2.setPaymentCurrency(Currency.USD);
        receivable2.setStatus(ReceivableStatus.AVAILABLE);

        when(receivableRepository.findAll())
                .thenReturn(List.of(receivable1, receivable2));

        List<ReceivableResponse> response = receivableService.listar();

        assertEquals(2, response.size());

        assertEquals(1L, response.get(0).id());
        assertEquals(1L, response.get(0).cedenteId());
        assertEquals(
                ReceivableType.DUPLICATA_MERCANTIL,
                response.get(0).type()
        );

        assertEquals(2L, response.get(1).id());
        assertEquals(1L, response.get(1).cedenteId());
        assertEquals(
                ReceivableType.CHEQUE_PRE_DATADO,
                response.get(1).type()
        );

        verify(receivableRepository).findAll();
    }

    @Test
    void deveBuscarReceivablePorId() {

        Cedente cedente = new Cedente();
        cedente.setId(1L);
        cedente.setName("Empresa Teste");
        cedente.setDocument("12345678000199");

        Receivable receivable = new Receivable();
        receivable.setId(1L);
        receivable.setCedente(cedente);
        receivable.setType(ReceivableType.DUPLICATA_MERCANTIL);
        receivable.setFaceValue(new BigDecimal("100000.00"));
        receivable.setTerm(3);
        receivable.setPaymentCurrency(Currency.BRL);
        receivable.setStatus(ReceivableStatus.AVAILABLE);

        when(receivableRepository.findById(1L))
                .thenReturn(Optional.of(receivable));

        ReceivableResponse response = receivableService.buscarPorId(1L);

        assertEquals(1L, response.id());
        assertEquals(1L, response.cedenteId());
        assertEquals(
                ReceivableType.DUPLICATA_MERCANTIL,
                response.type()
        );
        assertEquals(
                new BigDecimal("100000.00"),
                response.faceValue()
        );
        assertEquals(3, response.term());
        assertEquals(Currency.BRL, response.paymentCurrency());
        assertEquals(ReceivableStatus.AVAILABLE, response.status());

        verify(receivableRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoQuandoReceivableNaoEncontrado() {

        Long id = 999L;

        when(receivableRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> receivableService.buscarPorId(id)
        );

        verify(receivableRepository).findById(id);
    }

    @Test
    void deveLancarExcecaoQuandoCedenteNaoEncontrado() {

        ReceivableRequest request = new ReceivableRequest(
                999L,
                ReceivableType.DUPLICATA_MERCANTIL,
                new BigDecimal("100000.00"),
                3,
                Currency.BRL
        );

        when(cedenteRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> receivableService.criar(request)
        );

        verify(cedenteRepository).findById(999L);
    }
}