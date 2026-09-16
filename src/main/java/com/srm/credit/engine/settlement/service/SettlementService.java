package com.srm.credit.engine.settlement.service;

import com.srm.credit.engine.exchange.entity.ExchangeRate;
import com.srm.credit.engine.exchange.exception.ExchangeRateNotFoundException;
import com.srm.credit.engine.exchange.repository.ExchangeRateRepository;
import com.srm.credit.engine.pricing.dto.PricingRequest;
import com.srm.credit.engine.pricing.dto.PricingResponse;
import com.srm.credit.engine.pricing.service.PricingService;
import com.srm.credit.engine.receivable.entity.Receivable;
import com.srm.credit.engine.receivable.enums.Currency;
import com.srm.credit.engine.receivable.enums.ReceivableStatus;
import com.srm.credit.engine.receivable.exception.ReceivableNotFoundException;
import com.srm.credit.engine.receivable.repository.ReceivableRepository;
import com.srm.credit.engine.settlement.dto.SettlementRequest;
import com.srm.credit.engine.settlement.dto.SettlementResponse;
import com.srm.credit.engine.settlement.entity.Settlement;
import com.srm.credit.engine.settlement.exception.SettlementNotFoundException;
import com.srm.credit.engine.settlement.repository.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SettlementService {

    private final ReceivableRepository receivableRepository;
    private final SettlementRepository settlementRepository;
    private final PricingService pricingService;
    private final ExchangeRateRepository exchangeRateRepository;

    public SettlementService(
            ReceivableRepository receivableRepository,
            SettlementRepository settlementRepository,
            PricingService pricingService,
            ExchangeRateRepository exchangeRateRepository
    ) {
        this.receivableRepository = receivableRepository;
        this.settlementRepository = settlementRepository;
        this.pricingService = pricingService;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Transactional
    public SettlementResponse settle(SettlementRequest request) {

        Receivable receivable = receivableRepository.findById(
                request.receivableId()
        ).orElseThrow(() ->
                new ReceivableNotFoundException("Recebível não encontrado")
        );

        if (receivable.getStatus() == ReceivableStatus.SETTLED) {

            Settlement existingSettlement =
                    settlementRepository.findByReceivableId(
                            request.receivableId()
                    ).orElseThrow(() ->
                            new SettlementNotFoundException(
                                    "Liquidação do recebível não encontrada"
                            )
                    );

            return toResponse(existingSettlement);
        }

        BigDecimal exchangeRate = null;

        if (request.currency() == Currency.USD) {

            exchangeRate = exchangeRateRepository.findAll()
                    .stream()
                    .filter(rate ->
                            rate.getFromCurrency() == Currency.USD
                                    && rate.getToCurrency() == Currency.BRL
                    )
                    .max((rate1, rate2) ->
                            rate1.getEffectiveAt()
                                    .compareTo(rate2.getEffectiveAt())
                    )
                    .map(ExchangeRate::getRate)
                    .orElseThrow(() ->
                            new ExchangeRateNotFoundException(
                                    "Cotação USD/BRL não encontrada"
                            )
                    );
        }

        PricingRequest pricingRequest = new PricingRequest(
                receivable.getType(),
                receivable.getFaceValue(),
                receivable.getTerm(),
                request.currency(),
                exchangeRate
        );

        PricingResponse pricingResponse = pricingService.calculate(
                pricingRequest
        );

        Settlement settlement = new Settlement();

        settlement.setReceivable(receivable);
        settlement.setAmount(pricingResponse.presentValue());
        settlement.setCurrency(request.currency());
        settlement.setFxRateUsed(exchangeRate);
        settlement.setSettledAt(LocalDateTime.now());

        Settlement savedSettlement = settlementRepository.save(settlement);

        receivable.setStatus(ReceivableStatus.SETTLED);
        receivableRepository.save(receivable);

        return toResponse(savedSettlement);
    }

    @Transactional(readOnly = true)
    public List<SettlementResponse> findAll(
            Currency currency,
            Long cedenteId,
            LocalDateTime from,
            LocalDateTime to
    ) {

        List<Settlement> settlements = settlementRepository.findAll();

        return settlements.stream()
                .filter(settlement ->
                        currency == null
                                || settlement.getCurrency() == currency
                )
                .filter(settlement ->
                        cedenteId == null
                                || settlement.getReceivable()
                                .getCedente()
                                .getId()
                                .equals(cedenteId)
                )
                .filter(settlement ->
                        from == null
                                || !settlement.getSettledAt().isBefore(from)
                )
                .filter(settlement ->
                        to == null
                                || !settlement.getSettledAt().isAfter(to)
                )
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SettlementResponse findById(Long id) {

        Settlement settlement = settlementRepository.findById(id)
                .orElseThrow(() ->
                        new SettlementNotFoundException("Liquidação não encontrada")
                );

        return toResponse(settlement);
    }

    private SettlementResponse toResponse(Settlement settlement) {
        return new SettlementResponse(
                settlement.getId(),
                settlement.getReceivable().getId(),
                settlement.getAmount().setScale(2, RoundingMode.HALF_EVEN),
                settlement.getCurrency(),
                settlement.getFxRateUsed() != null
                        ? settlement.getFxRateUsed().setScale(4, RoundingMode.HALF_EVEN)
                        : null,
                settlement.getSettledAt()
        );
    }
}