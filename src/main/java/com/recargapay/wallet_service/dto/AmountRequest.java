package com.recargapay.wallet_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AmountRequest(
        @NotNull
        @DecimalMin(value = "0.01", inclusive = true, message = "amount must be greater than zero")
        BigDecimal amount
) {}
