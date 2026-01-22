package com.recargapay.wallet_service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID transferId,
        UUID fromWalletId,
        UUID toWalletId,
        BigDecimal amount,
        BigDecimal fromBalance,
        BigDecimal toBalance,
        Instant occurredAt
) {}
