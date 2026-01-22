package com.recargapay.wallet_service.service;

import com.recargapay.wallet_service.domain.LedgerEntry;
import com.recargapay.wallet_service.domain.Wallet;
import com.recargapay.wallet_service.exception.InsufficientFundsException;
import com.recargapay.wallet_service.repository.LedgerEntryRepository;
import com.recargapay.wallet_service.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferServiceTest {

    private WalletRepository walletRepository;
    private LedgerEntryRepository ledgerEntryRepository;
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        ledgerEntryRepository = mock(LedgerEntryRepository.class);
        transferService = new TransferService(walletRepository, ledgerEntryRepository);
    }

    @Test
    void shouldTransferAmountAtomically() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        Wallet from = Wallet.builder().id(fromId).userId("u1").balance(new BigDecimal("10.00")).build();
        Wallet to = Wallet.builder().id(toId).userId("u2").balance(new BigDecimal("5.00")).build();

        when(walletRepository.findById(fromId)).thenReturn(Optional.of(from));
        when(walletRepository.findById(toId)).thenReturn(Optional.of(to));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerEntryRepository.save(any(LedgerEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = transferService.transfer(fromId, toId, new BigDecimal("3.50"), "t1");

        assertThat(from.getBalance()).isEqualByComparingTo("6.50");
        assertThat(to.getBalance()).isEqualByComparingTo("8.50");
        verify(ledgerEntryRepository, times(2)).save(any(LedgerEntry.class));
        assertThat(result.transferId()).isNotNull();
    }

    @Test
    void shouldFailWhenInsufficientFunds() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        Wallet from = Wallet.builder().id(fromId).userId("u1").balance(new BigDecimal("1.00")).build();
        Wallet to = Wallet.builder().id(toId).userId("u2").balance(new BigDecimal("5.00")).build();

        when(walletRepository.findById(fromId)).thenReturn(Optional.of(from));
        when(walletRepository.findById(toId)).thenReturn(Optional.of(to));

        assertThatThrownBy(() -> transferService.transfer(fromId, toId, new BigDecimal("2.00"), "t2"))
                .isInstanceOf(InsufficientFundsException.class);

        verify(walletRepository, never()).save(any());
        verify(ledgerEntryRepository, never()).save(any());
    }

    @Test
    void shouldRejectSameWalletTransfer() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> transferService.transfer(id, id, new BigDecimal("1.00"), "t3"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
