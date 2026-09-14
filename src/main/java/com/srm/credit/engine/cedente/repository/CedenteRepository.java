package com.srm.credit.engine.cedente.repository;

import com.srm.credit.engine.cedente.entity.Cedente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CedenteRepository extends JpaRepository<Cedente, Long> {
}