package com.srm.credit.engine.exchange.repository;

import com.srm.credit.engine.exchange.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
}