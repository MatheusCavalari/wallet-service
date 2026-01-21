package com.recargapay.wallet_service.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ledger_entry")
public class LedgerEntry {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "wallet_id", nullable = false, columnDefinition = "uuid")
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount; // always positive

    @Column(name = "transfer_id", columnDefinition = "uuid")
    private UUID transferId;

    @Column(name = "related_wallet_id", columnDefinition = "uuid")
    private UUID relatedWalletId;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
