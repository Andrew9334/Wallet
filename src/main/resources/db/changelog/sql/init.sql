create table if not exists wallet
(
    wallet_id      uuid primary key,
    operation_type varchar(50),
    amount         NUMERIC(5, 2)
);