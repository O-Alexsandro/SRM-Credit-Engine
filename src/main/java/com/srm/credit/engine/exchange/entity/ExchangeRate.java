package com.srm.credit.engine.exchange.entity;

import com.srm.credit.engine.receivable.enums.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rates")
@Getter
@Setter
@NoArgsConstructor
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_currency", nullable = false)
    private Currency fromCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_currency", nullable = false)
    private Currency toCurrency;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;

    @Column(name = "effective_at", nullable = false)
    private LocalDateTime effectiveAt;
}