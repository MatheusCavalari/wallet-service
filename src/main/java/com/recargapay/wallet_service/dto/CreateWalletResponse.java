package com.recargapay.wallet_service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateWalletResponse(
        UUID walletId,
        String userId,
        BigDecimal balance
) {}
