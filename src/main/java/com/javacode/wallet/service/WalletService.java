package com.javacode.wallet.service;

import com.javacode.wallet.exception.InsufficientFundsException;
import com.javacode.wallet.exception.WalletNotFoundException;
import com.javacode.wallet.model.OperationType;
import com.javacode.wallet.model.Wallet;
import com.javacode.wallet.model.WalletRequest;
import com.javacode.wallet.repository.WalletRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    @Retryable(maxAttempts = 3, include = OptimisticLockingFailureException.class)
    public void processOperation(WalletRequest walletRequest) {
        Wallet wallet = walletRepository.findById(walletRequest.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet with ID" + walletRequest.getWalletId() + " not found"));

        try {
            if (walletRequest.getOperationType() == OperationType.DEPOSIT) {
                wallet.setAmount(wallet.getAmount().add(walletRequest.getAmount()));
            } else if (walletRequest.getOperationType() == OperationType.WITHDRAW) {
                if (wallet.getAmount().compareTo(walletRequest.getAmount()) < 0) {
                    throw new InsufficientFundsException("Insufficient funds in wallet with ID " + walletRequest.getWalletId());
                }
                wallet.setAmount(wallet.getAmount().subtract(walletRequest.getAmount()));
            }

            walletRepository.save(wallet);

        } catch (OptimisticLockingFailureException e) {
            throw new RuntimeException("Optimistic locking failure while processing operation for wallet with ID "
                    + walletRequest.getWalletId(), e);
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
        return wallet.getAmount();
    }
}
