package com.jpmonitor.domains.finance.repository;

import com.jpmonitor.domains.finance.entity.CashAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CashAccountRepository extends JpaRepository<CashAccount, UUID> {
    Optional<CashAccount> findByCode(String code);
}