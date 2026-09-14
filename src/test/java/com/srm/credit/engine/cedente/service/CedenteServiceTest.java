package com.srm.credit.engine.cedente.service;

import com.srm.credit.engine.cedente.dto.CedenteRequest;
import com.srm.credit.engine.cedente.dto.CedenteResponse;
import com.srm.credit.engine.cedente.entity.Cedente;
import com.srm.credit.engine.cedente.repository.CedenteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CedenteServiceTest {

    @Mock
    private CedenteRepository cedenteRepository;

    @InjectMocks
    private CedenteService cedenteService;

    @Test
    void deveCriarCedente() {

        CedenteRequest request = new CedenteRequest(
                "Empresa Teste",
                "12345678000199"
        );

        Cedente cedenteSalvo = new Cedente();
        cedenteSalvo.setId(1L);
        cedenteSalvo.setName("Empresa Teste");
        cedenteSalvo.setDocument("12345678000199");

        when(cedenteRepository.save(any(Cedente.class)))
                .thenReturn(cedenteSalvo);

        CedenteResponse response = cedenteService.criar(request);

        assertEquals(1L, response.id());
        assertEquals("Empresa Teste", response.name());
        assertEquals("12345678000199", response.document());

        verify(cedenteRepository).save(any(Cedente.class));
    }

    @Test
    void deveListarCedentes() {

        Cedente cedente1 = new Cedente();
        cedente1.setId(1L);
        cedente1.setName("Empresa 1");
        cedente1.setDocument("11111111000111");

        Cedente cedente2 = new Cedente();
        cedente2.setId(2L);
        cedente2.setName("Empresa 2");
        cedente2.setDocument("22222222000122");

        when(cedenteRepository.findAll())
                .thenReturn(List.of(cedente1, cedente2));

        List<CedenteResponse> response = cedenteService.listar();

        assertEquals(2, response.size());

        assertEquals(1L, response.get(0).id());
        assertEquals("Empresa 1", response.get(0).name());
        assertEquals("11111111000111", response.get(0).document());

        assertEquals(2L, response.get(1).id());
        assertEquals("Empresa 2", response.get(1).name());
        assertEquals("22222222000122", response.get(1).document());

        verify(cedenteRepository).findAll();
    }

    @Test
    void deveBuscarCedentePorId() {

        Long id = 1L;

        Cedente cedente = new Cedente();
        cedente.setId(id);
        cedente.setName("Empresa Teste");
        cedente.setDocument("12345678000199");

        when(cedenteRepository.findById(id))
                .thenReturn(Optional.of(cedente));

        CedenteResponse response = cedenteService.buscarPorId(id);

        assertEquals(1L, response.id());
        assertEquals("Empresa Teste", response.name());
        assertEquals("12345678000199", response.document());

        verify(cedenteRepository).findById(id);
    }

    @Test
    void deveLancarExcecaoQuandoCedenteNaoEncontrado() {

        Long id = 999L;

        when(cedenteRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> cedenteService.buscarPorId(id)
        );

        verify(cedenteRepository).findById(id);
    }
}