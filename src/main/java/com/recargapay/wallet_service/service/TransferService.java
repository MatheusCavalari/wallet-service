package com.recargapay.wallet_service.service;

import com.recargapay.wallet_service.domain.LedgerEntry;
import com.recargapay.wallet_service.domain.LedgerType;
import com.recargapay.wallet_service.domain.Wallet;
import com.recargapay.wallet_service.exception.InsufficientFundsException;
import com.recargapay.wallet_service.exception.NotFoundException;
import com.recargapay.wallet_service.repository.LedgerEntryRepository;
import com.recargapay.wallet_service.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    public TransferResult transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount, String idempotencyKey) {
        if (fromWalletId.equals(toWalletId)) {
            throw new IllegalArgumentException("fromWalletId and toWalletId must be different");
        }

        Wallet from = walletRepository.findById(fromWalletId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + fromWalletId));
        Wallet to = walletRepository.findById(toWalletId)
                .orElseThrow(() -> new NotFoundException("Wallet not found: " + toWalletId));

        BigDecimal newFromBalance = from.getBalance().subtract(amount);
        if (newFromBalance.signum() < 0) {
            throw new InsufficientFundsException("Insufficient funds for wallet: " + fromWalletId);
        }

        from.setBalance(newFromBalance);
        to.setBalance(to.getBalance().add(amount));

        // persist wallets first (optimistic lock)
        walletRepository.save(from);
        walletRepository.save(to);

        UUID transferId = UUID.randomUUID();
        Instant now = Instant.now();

        LedgerEntry out = LedgerEntry.builder()
                .id(UUID.randomUUID())
                .walletId(fromWalletId)
                .type(LedgerType.TRANSFER_OUT)
                .amount(amount)
                .transferId(transferId)
                .relatedWalletId(toWalletId)
                .idempotencyKey(idempotencyKey)
                .createdAt(now)
                .build();

        LedgerEntry in = LedgerEntry.builder()
                .id(UUID.randomUUID())
                .walletId(toWalletId)
                .type(LedgerType.TRANSFER_IN)
                .amount(amount)
                .transferId(transferId)
                .relatedWalletId(fromWalletId)
                .idempotencyKey(idempotencyKey)
                .createdAt(now)
                .build();

        ledgerEntryRepository.save(out);
        ledgerEntryRepository.save(in);

        return new TransferResult(transferId, now, from.getBalance(), to.getBalance());
    }

    public record TransferResult(UUID transferId, Instant occurredAt, BigDecimal fromBalance, BigDecimal toBalance) {}
}
