package com.jpm.erp.domains.finance.repository;

import com.jpm.erp.domains.finance.entity.AccountsPayable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountsPayableRepository extends JpaRepository<AccountsPayable, UUID> {
    Optional<AccountsPayable> findByApNumber(String apNumber);
}