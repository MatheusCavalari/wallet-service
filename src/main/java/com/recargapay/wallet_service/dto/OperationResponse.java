package com.recargapay.wallet_service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OperationResponse(
        UUID walletId,
        UUID ledgerEntryId,
        BigDecimal balance,
        Instant occurredAt
) {}
