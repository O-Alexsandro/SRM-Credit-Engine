package com.srm.credit.engine.pricing.controller;

import com.srm.credit.engine.pricing.dto.PricingRequest;
import com.srm.credit.engine.pricing.dto.PricingResponse;
import com.srm.credit.engine.pricing.service.PricingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pricing")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping
    public PricingResponse calculate(@Valid @RequestBody PricingRequest request) {
        return pricingService.calculate(request);
    }
}