package com.recargapay.wallet_service.service;

import com.recargapay.wallet_service.domain.LedgerEntry;
import com.recargapay.wallet_service.domain.LedgerType;
import com.recargapay.wallet_service.domain.Wallet;
import com.recargapay.wallet_service.exception.InsufficientFundsException;
import com.recargapay.wallet_service.exception.NotFoundException;
import com.recargapay.wallet_service.repository.LedgerEntryRepository;
import com.recargapay.wallet_service.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public Wallet createWallet(String userId) {
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .balance(new BigDecimal("0.00"))
                .createdAt(Instant.now())
                .build();

        return walletRepository.save(wallet);
    }

    public Wallet getWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + walletId));
    }

    @Transactional
    public LedgerEntry deposit(UUID walletId, BigDecimal amount, String idempotencyKey) {
        Wallet wallet = getWallet(walletId);

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet); // triggers optimistic locking

        LedgerEntry entry = LedgerEntry.builder()
                .id(UUID.randomUUID())
                .walletId(walletId)
                .type(LedgerType.DEPOSIT)
                .amount(amount)
                .idempotencyKey(idempotencyKey)
                .createdAt(Instant.now())
                .build();

        return ledgerEntryRepository.save(entry);
    }

    @Transactional
    public LedgerEntry withdraw(UUID walletId, BigDecimal amount, String idempotencyKey) {
        Wallet wallet = getWallet(walletId);

        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        if (newBalance.signum() < 0) {
            throw new InsufficientFundsException("Insufficient funds for wallet: " + walletId);
        }

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        LedgerEntry entry = LedgerEntry.builder()
                .id(UUID.randomUUID())
                .walletId(walletId)
                .type(LedgerType.WITHDRAW)
                .amount(amount)
                .idempotencyKey(idempotencyKey)
                .createdAt(Instant.now())
                .build();

        return ledgerEntryRepository.save(entry);
    }
}