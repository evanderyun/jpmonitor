package com.jpm.erp.domains.inventory.repository;

import com.jpm.erp.domains.inventory.entity.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SparePartRepository extends JpaRepository<SparePart, UUID> {
    Optional<SparePart> findByPartNumber(String partNumber);
}