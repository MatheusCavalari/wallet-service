package com.recargapay.wallet_service.service;


import com.recargapay.wallet_service.domain.LedgerEntry;
import com.recargapay.wallet_service.domain.LedgerType;
import com.recargapay.wallet_service.domain.Wallet;
import com.recargapay.wallet_service.exception.InsufficientFundsException;
import com.recargapay.wallet_service.exception.NotFoundException;
import com.recargapay.wallet_service.repository.LedgerEntryRepository;
import com.recargapay.wallet_service.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    private WalletRepository walletRepository;
    private LedgerEntryRepository ledgerEntryRepository;
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        ledgerEntryRepository = mock(LedgerEntryRepository.class);
        walletService = new WalletService(walletRepository, ledgerEntryRepository);
    }

    @Test
    void shouldCreateWalletWithZeroBalance() {
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Wallet wallet = walletService.createWallet("user-123");

        assertThat(wallet.getId()).isNotNull();
        assertThat(wallet.getBalance()).isEqualByComparingTo("0.00");
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenWalletDoesNotExist() {
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.getWallet(walletId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void depositShouldIncreaseBalanceAndCreateLedgerEntry() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = Wallet.builder()
                .id(walletId)
                .userId("user-1")
                .balance(new BigDecimal("10.00"))
                .build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerEntryRepository.save(any(LedgerEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LedgerEntry entry = walletService.deposit(walletId, new BigDecimal("2.50"), "k1");

        assertThat(wallet.getBalance()).isEqualByComparingTo("12.50");
        assertThat(entry.getType()).isEqualTo(LedgerType.DEPOSIT);
        assertThat(entry.getAmount()).isEqualByComparingTo("2.50");
        assertThat(entry.getWalletId()).isEqualTo(walletId);
        verify(ledgerEntryRepository).save(any(LedgerEntry.class));
    }

    @Test
    void withdrawShouldDecreaseBalanceAndCreateLedgerEntry() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = Wallet.builder()
                .id(walletId)
                .userId("user-1")
                .balance(new BigDecimal("10.00"))
                .build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerEntryRepository.save(any(LedgerEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LedgerEntry entry = walletService.withdraw(walletId, new BigDecimal("3.00"), "k2");

        assertThat(wallet.getBalance()).isEqualByComparingTo("7.00");
        assertThat(entry.getType()).isEqualTo(LedgerType.WITHDRAW);
        assertThat(entry.getAmount()).isEqualByComparingTo("3.00");
        verify(ledgerEntryRepository).save(any(LedgerEntry.class));
    }

    @Test
    void withdrawShouldFailWhenInsufficientFunds() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = Wallet.builder()
                .id(walletId)
                .userId("user-1")
                .balance(new BigDecimal("1.00"))
                .build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.withdraw(walletId, new BigDecimal("2.00"), "k3"))
                .isInstanceOf(InsufficientFundsException.class);

        verify(ledgerEntryRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
    }
}