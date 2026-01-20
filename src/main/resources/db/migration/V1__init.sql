CREATE TABLE wallet (
                        id UUID PRIMARY KEY,
                        user_id TEXT NOT NULL,
                        balance NUMERIC(19,2) NOT NULL DEFAULT 0,
                        version BIGINT NOT NULL DEFAULT 0,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ledger_entry (
                              id UUID PRIMARY KEY,
                              wallet_id UUID NOT NULL REFERENCES wallet(id),
                              type TEXT NOT NULL,
                              amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
                              transfer_id UUID NULL,
                              related_wallet_id UUID NULL,
                              idempotency_key TEXT NULL,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ledger_wallet_time ON ledger_entry(wallet_id, created_at);
CREATE INDEX idx_ledger_transfer_id ON ledger_entry(transfer_id);

CREATE UNIQUE INDEX ux_ledger_wallet_idempotency
    ON ledger_entry(wallet_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;
