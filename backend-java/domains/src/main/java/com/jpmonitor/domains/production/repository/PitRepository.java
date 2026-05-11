package com.jpmonitor.domains.production.repository;

import com.jpmonitor.domains.production.entity.Pit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PitRepository extends JpaRepository<Pit, UUID> {
    Optional<Pit> findByCode(String code);
}