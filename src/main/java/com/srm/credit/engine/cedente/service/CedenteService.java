package com.srm.credit.engine.cedente.service;

import com.srm.credit.engine.cedente.entity.Cedente;
import com.srm.credit.engine.cedente.exception.CedenteNotFoundException;
import com.srm.credit.engine.cedente.repository.CedenteRepository;
import com.srm.credit.engine.cedente.dto.CedenteRequest;
import com.srm.credit.engine.cedente.dto.CedenteResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CedenteService {

    private final CedenteRepository cedenteRepository;

    public CedenteService(CedenteRepository cedenteRepository) {
        this.cedenteRepository = cedenteRepository;
    }

    public CedenteResponse criar(CedenteRequest request) {

        Cedente cedente = new Cedente();

        cedente.setName(request.name());
        cedente.setDocument(request.document());

        Cedente cedenteSalvo = cedenteRepository.save(cedente);

        return toResponse(cedenteSalvo);
    }

    public List<CedenteResponse> listar() {

        return cedenteRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CedenteResponse buscarPorId(Long id) {

        Cedente cedente = cedenteRepository.findById(id)
                .orElseThrow(() -> new CedenteNotFoundException("Cedente não encontrado"));

        return toResponse(cedente);
    }

    private CedenteResponse toResponse(Cedente cedente) {

        return new CedenteResponse(
                cedente.getId(),
                cedente.getName(),
                cedente.getDocument()
        );
    }
}
