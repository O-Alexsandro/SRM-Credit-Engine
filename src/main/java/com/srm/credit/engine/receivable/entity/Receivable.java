package com.srm.credit.engine.receivable.entity;

import com.srm.credit.engine.cedente.entity.Cedente;
import com.srm.credit.engine.receivable.enums.Currency;
import com.srm.credit.engine.receivable.enums.ReceivableStatus;
import com.srm.credit.engine.receivable.enums.ReceivableType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "receivables")
@Getter
@Setter
@NoArgsConstructor
public class Receivable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cedente_id", nullable = false)
    private Cedente cedente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceivableType type;

    @Column(name = "face_value", precision = 19, scale = 6, nullable = false)
    private BigDecimal faceValue;

    @Column(nullable = false)
    private Integer term;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_currency", nullable = false)
    private Currency paymentCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceivableStatus status;
}