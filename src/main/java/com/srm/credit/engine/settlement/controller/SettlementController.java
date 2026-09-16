package com.srm.credit.engine.settlement.controller;

import com.srm.credit.engine.receivable.enums.Currency;
import com.srm.credit.engine.settlement.dto.SettlementRequest;
import com.srm.credit.engine.settlement.dto.SettlementResponse;
import com.srm.credit.engine.settlement.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SettlementResponse settle(
            @Valid @RequestBody SettlementRequest request
    ) {
        return settlementService.settle(request);
    }

    @GetMapping
    public List<SettlementResponse> findAll(
            @RequestParam(required = false) Currency currency,
            @RequestParam(required = false) Long cedenteId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to
    ) {
        return settlementService.findAll(
                currency,
                cedenteId,
                from,
                to
        );
    }

    @GetMapping("/{id}")
    public SettlementResponse findById(@PathVariable Long id) {
        return settlementService.findById(id);
    }
}