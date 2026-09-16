package com.srm.credit.engine.settlement.entity;

import com.srm.credit.engine.receivable.entity.Receivable;
import com.srm.credit.engine.receivable.enums.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
@Getter
@Setter
@NoArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "receivable_id", nullable = false, unique = true)
    private Receivable receivable;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(name = "fx_rate_used", precision = 19, scale = 6)
    private BigDecimal fxRateUsed;

    @Column(name = "settled_at", nullable = false)
    private LocalDateTime settledAt;
}