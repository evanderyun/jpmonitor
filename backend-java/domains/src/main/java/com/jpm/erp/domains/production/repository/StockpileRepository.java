package com.jpm.erp.domains.production.repository;

import com.jpm.erp.domains.production.entity.Stockpile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockpileRepository extends JpaRepository<Stockpile, UUID> {
    Optional<Stockpile> findByCode(String code);
}
