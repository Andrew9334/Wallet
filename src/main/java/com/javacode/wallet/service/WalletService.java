package com.javacode.wallet.service;

import com.javacode.wallet.exception.InsufficientFundsException;
import com.javacode.wallet.exception.WalletNotFoundException;
import com.javacode.wallet.model.OperationType;
import com.javacode.wallet.model.Wallet;
import com.javacode.wallet.model.WalletRequest;
import com.javacode.wallet.repository.WalletRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public synchronized void processOperation(WalletRequest walletRequest) {
        try {
            Wallet wallet = walletRepository.findById(walletRequest.getWalletId())
                    .orElseThrow(() -> new WalletNotFoundException("Wallet with ID" + walletRequest.getWalletId() + " not found"));

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
            throw new ConcurrentModificationException("Wallet has been modified by another transaction. Please try again.");
        }
    }

    @Transactional(readOnly = true)
    public synchronized BigDecimal getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
        return wallet.getAmount();
    }
}
