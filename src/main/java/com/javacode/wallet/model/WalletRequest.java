package com.javacode.wallet.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@Accessors(chain = true)
public class WalletRequest {

    private UUID walletId;

    private OperationType operationType;

    private BigDecimal amount;
}
