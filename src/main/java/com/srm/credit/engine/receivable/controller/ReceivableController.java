package com.srm.credit.engine.receivable.controller;

import com.srm.credit.engine.receivable.dto.ReceivableRequest;
import com.srm.credit.engine.receivable.dto.ReceivableResponse;
import com.srm.credit.engine.receivable.service.ReceivableService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/receivables")
public class ReceivableController {

    private final ReceivableService receivableService;

    public ReceivableController(ReceivableService receivableService) {
        this.receivableService = receivableService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReceivableResponse criar(
            @Valid @RequestBody ReceivableRequest request
    ) {
        return receivableService.criar(request);
    }

    @GetMapping
    public List<ReceivableResponse> listar() {
        return receivableService.listar();
    }

    @GetMapping("/{id}")
    public ReceivableResponse buscarPorId(
            @PathVariable Long id
    ) {
        return receivableService.buscarPorId(id);
    }
}