package com.jpmonitor.domains.finance.repository;

import com.jpmonitor.domains.finance.entity.CostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CostCategoryRepository extends JpaRepository<CostCategory, UUID> {
    Optional<CostCategory> findByCode(String code);
}