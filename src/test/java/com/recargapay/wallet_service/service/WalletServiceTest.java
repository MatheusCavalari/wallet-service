package com.recargapay.wallet_service.service;

import com.recargapay.wallet_service.domain.Wallet;
import com.recargapay.wallet_service.exception.NotFoundException;
import com.recargapay.wallet_service.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    private WalletRepository walletRepository;
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        walletService = new WalletService(walletRepository);
    }

    @Test
    void shouldCreateWalletWithZeroBalance() {
        // given
        String userId = "user-123";

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        when(walletRepository.save(any(Wallet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Wallet wallet = walletService.createWallet(userId);

        // then
        verify(walletRepository).save(walletCaptor.capture());
        Wallet savedWallet = walletCaptor.getValue();

        assertThat(savedWallet.getId()).isNotNull();
        assertThat(savedWallet.getUserId()).isEqualTo(userId);
        assertThat(savedWallet.getBalance()).isEqualByComparingTo("0.00");
        assertThat(savedWallet.getCreatedAt()).isNotNull();

        assertThat(wallet.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldReturnWalletWhenItExists() {
        // given
        UUID walletId = UUID.randomUUID();
        Wallet wallet = Wallet.builder()
                .id(walletId)
                .userId("user-123")
                .balance(new BigDecimal("10.00"))
                .build();

        when(walletRepository.findById(walletId))
                .thenReturn(Optional.of(wallet));

        // when
        Wallet result = walletService.getWallet(walletId);

        // then
        assertThat(result).isSameAs(wallet);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenWalletDoesNotExist() {
        // given
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> walletService.getWallet(walletId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(walletId.toString());
    }
}
