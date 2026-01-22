package com.recargapay.wallet_service.repository;

import com.recargapay.wallet_service.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    @Query("""
        SELECT COALESCE(SUM(
          CASE
            WHEN e.type IN ('DEPOSIT', 'TRANSFER_IN') THEN e.amount
            ELSE -e.amount
          END
        ), 0)
        FROM LedgerEntry e
        WHERE e.walletId = :walletId
          AND e.createdAt <= :at
    """)
    java.math.BigDecimal calculateBalanceAt(@Param("walletId") UUID walletId, @Param("at") Instant at);
}