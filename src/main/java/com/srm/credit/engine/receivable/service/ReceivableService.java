package com.srm.credit.engine.receivable.service;

import com.srm.credit.engine.cedente.entity.Cedente;
import com.srm.credit.engine.cedente.repository.CedenteRepository;
import com.srm.credit.engine.receivable.dto.ReceivableRequest;
import com.srm.credit.engine.receivable.dto.ReceivableResponse;
import com.srm.credit.engine.receivable.entity.Receivable;
import com.srm.credit.engine.receivable.enums.ReceivableStatus;
import com.srm.credit.engine.receivable.repository.ReceivableRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReceivableService {

    private final ReceivableRepository receivableRepository;
    private final CedenteRepository cedenteRepository;

    public ReceivableService(
            ReceivableRepository receivableRepository,
            CedenteRepository cedenteRepository
    ) {
        this.receivableRepository = receivableRepository;
        this.cedenteRepository = cedenteRepository;
    }

    public ReceivableResponse criar(ReceivableRequest request) {

        Cedente cedente = cedenteRepository.findById(request.cedenteId())
                .orElseThrow(() -> new RuntimeException("Cedente não encontrado"));

        Receivable receivable = new Receivable();

        receivable.setCedente(cedente);
        receivable.setType(request.type());
        receivable.setFaceValue(request.faceValue());
        receivable.setTerm(request.term());
        receivable.setPaymentCurrency(request.paymentCurrency());
        receivable.setStatus(ReceivableStatus.AVAILABLE);

        Receivable receivableSalvo = receivableRepository.save(receivable);

        return toResponse(receivableSalvo);
    }

    public List<ReceivableResponse> listar() {

        return receivableRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ReceivableResponse buscarPorId(Long id) {

        Receivable receivable = receivableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recebível não encontrado"));

        return toResponse(receivable);
    }

    private ReceivableResponse toResponse(Receivable receivable) {

        return new ReceivableResponse(
                receivable.getId(),
                receivable.getCedente().getId(),
                receivable.getType(),
                receivable.getFaceValue(),
                receivable.getTerm(),
                receivable.getPaymentCurrency(),
                receivable.getStatus()
        );
    }
}