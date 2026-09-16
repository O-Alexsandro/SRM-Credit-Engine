package com.srm.credit.engine.settlement.repository;

import com.srm.credit.engine.receivable.enums.Currency;
import com.srm.credit.engine.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Optional<Settlement> findByReceivableId(Long receivableId);

    List<Settlement> findByCurrency(Currency currency);

    List<Settlement> findBySettledAtBetween(
            LocalDateTime from,
            LocalDateTime to
    );

    List<Settlement> findByReceivableCedenteId(Long cedenteId);

}