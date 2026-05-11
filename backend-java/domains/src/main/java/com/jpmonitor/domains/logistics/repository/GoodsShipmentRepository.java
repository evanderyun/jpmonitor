package com.jpmonitor.domains.logistics.repository;

import com.jpmonitor.domains.logistics.entity.GoodsShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoodsShipmentRepository extends JpaRepository<GoodsShipment, UUID> {
    Optional<GoodsShipment> findByDoNumber(String doNumber);
}