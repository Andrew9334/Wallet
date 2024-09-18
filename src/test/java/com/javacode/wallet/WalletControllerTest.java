package com.javacode.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javacode.wallet.controller.WalletController;
import com.javacode.wallet.exception.InsufficientFundsException;
import com.javacode.wallet.exception.WalletNotFoundException;
import com.javacode.wallet.model.OperationType;
import com.javacode.wallet.model.WalletRequest;
import com.javacode.wallet.service.WalletService;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
public class WalletControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testHandleOperationDeposit() throws Exception {
        WalletRequest request = new WalletRequest()
                .setWalletId(UUID.fromString("8c43fa60-8311-456c-b07f-e29020ee91b9"))
                .setOperationType(OperationType.DEPOSIT)
                .setAmount(BigDecimal.valueOf(400));

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(walletService, times(1)).processOperation(any(WalletRequest.class));
    }

    @Test
    public void testHandleOperationWithdrawSuccess() throws Exception {
        WalletRequest request = new WalletRequest()
                .setWalletId(UUID.fromString("8c43fa60-8311-456c-b07f-e29020ee91b8"))
                .setOperationType(OperationType.WITHDRAW)
                .setAmount(BigDecimal.valueOf(400));

        doNothing().when(walletService).processOperation(request);

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(walletService, times(1)).processOperation(request);
    }

    @Test
    public void testHandleOperationWithdrawInsufficientFunds() throws Exception {
        WalletRequest request = new WalletRequest()
                .setWalletId(UUID.fromString("8c43fa60-8311-456c-b07f-e29020ee91b7"))
                .setOperationType(OperationType.DEPOSIT)
                .setAmount(BigDecimal.valueOf(400));

        doThrow(new InsufficientFundsException("Insufficient funds")).when(walletService).processOperation(request);

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient funds"));

        verify(walletService, times(1)).processOperation(request);
    }

    @Test
    public void testGetBalanceSuccess() throws Exception {
        WalletRequest request = new WalletRequest()
                .setWalletId(UUID.fromString("8c43fa60-8311-456c-b07f-e29020ee91b7"))
                .setOperationType(OperationType.DEPOSIT)
                .setAmount(BigDecimal.valueOf(400));

        when(walletService.getBalance(request.getWalletId())).thenReturn(request.getAmount());

        mockMvc.perform(get("/api/v1/wallets/wallets/{walletId}", request.getWalletId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(request.getAmount())));
    }

    @Test
    public void testGetBalanceNotFound() throws Exception {
        UUID walletId = UUID.randomUUID();

        when(walletService.getBalance(walletId)).thenThrow(new WalletNotFoundException("Wallet not found"));

        mockMvc.perform(get("/api/v1/wallets/wallets/{walletId}", walletId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testHandleOptimisticLocking() throws Exception {
        WalletRequest request = new WalletRequest()
                .setWalletId(UUID.fromString("8c43fa60-8311-456c-b07f-e29020ee91b9"))
                .setOperationType(OperationType.WITHDRAW)
                .setAmount(BigDecimal.valueOf(100));

        doThrow(new OptimisticLockException("Conflict occurred: Version mismatch")).when(walletService).processOperation(request);

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Conflict occurred: Version mismatch"));

        verify(walletService, times(1)).processOperation(request);
    }
}
