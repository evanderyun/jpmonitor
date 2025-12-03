package com.jpm.erp.domains.fleet.repository;

import com.jpm.erp.domains.fleet.entity.UnitMutation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnitMutationRepository extends JpaRepository<UnitMutation, UUID> {
    Optional<UnitMutation> findByMutationNumber(String mutationNumber);
}
