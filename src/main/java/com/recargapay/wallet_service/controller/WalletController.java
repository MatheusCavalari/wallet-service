package com.recargapay.wallet_service.controller;

import com.recargapay.wallet_service.domain.Wallet;
import com.recargapay.wallet_service.dto.CreateWalletRequest;
import com.recargapay.wallet_service.dto.CreateWalletResponse;
import com.recargapay.wallet_service.dto.WalletBalanceResponse;
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
}
