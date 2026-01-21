package com.recargapay.wallet_service.controller;

import com.recargapay.wallet_service.domain.LedgerEntry;
import com.recargapay.wallet_service.domain.Wallet;
import com.recargapay.wallet_service.dto.*;
import com.recargapay.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateWalletResponse create(@Valid @RequestBody CreateWalletRequest request) {
        Wallet wallet = walletService.createWallet(request.userId());
        return new CreateWalletResponse(wallet.getId(), wallet.getUserId(), wallet.getBalance());
    }

    @GetMapping("/{walletId}/balance")
    public WalletBalanceResponse balance(@PathVariable UUID walletId) {
        Wallet wallet = walletService.getWallet(walletId);
        return new WalletBalanceResponse(wallet.getId(), wallet.getBalance(), "now");
    }

    @PostMapping("/{walletId}/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public OperationResponse deposit(
            @PathVariable UUID walletId,
            @Valid @RequestBody AmountRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        LedgerEntry entry = walletService.deposit(walletId, request.amount(), idempotencyKey);
        Wallet wallet = walletService.getWallet(walletId);

        return new OperationResponse(walletId, entry.getId(), wallet.getBalance(), entry.getCreatedAt());
    }

    @PostMapping("/{walletId}/withdraw")
    @ResponseStatus(HttpStatus.CREATED)
    public OperationResponse withdraw(
            @PathVariable UUID walletId,
            @Valid @RequestBody AmountRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        LedgerEntry entry = walletService.withdraw(walletId, request.amount(), idempotencyKey);
        Wallet wallet = walletService.getWallet(walletId);

        return new OperationResponse(walletId, entry.getId(), wallet.getBalance(), entry.getCreatedAt());
    }
}