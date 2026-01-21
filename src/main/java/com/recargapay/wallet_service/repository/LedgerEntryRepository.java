package com.recargapay.wallet_service.repository;

import com.recargapay.wallet_service.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
}