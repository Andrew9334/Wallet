CREATE TABLE IF NOT EXISTS wallet
(
    wallet_id      UUID PRIMARY KEY,
    operation_type VARCHAR(50),
    amount         NUMERIC(5, 2)
);