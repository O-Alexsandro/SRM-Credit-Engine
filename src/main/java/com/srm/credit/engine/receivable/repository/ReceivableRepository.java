package com.srm.credit.engine.receivable.repository;

import com.srm.credit.engine.receivable.entity.Receivable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceivableRepository extends JpaRepository<Receivable, Long> {
}