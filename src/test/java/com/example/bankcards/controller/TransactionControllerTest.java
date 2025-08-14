package com.example.bankcards.controller;

import com.example.bankcards.dto.transaction.balance.ResponseBalanceDto;
import com.example.bankcards.dto.transaction.transfer.RequestTransferDto;
import com.example.bankcards.dto.transaction.transfer.ResponseTransferDto;
import com.example.bankcards.exception.NotEnoughBalanceException;
import com.example.bankcards.security.config.JwtAuthenticationEntryPoint;
import com.example.bankcards.security.filter.JwtAuthenticationFilter;
import com.example.bankcards.security.service.CustomUserDetailsService;
import com.example.bankcards.security.service.JwtService;
import com.example.bankcards.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private TransactionService transactionService;

    @Test
    void transfer_success() throws Exception {

        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        RequestTransferDto request = RequestTransferDto.builder()
                .fromCard(fromCardId)
                .toCard(toCardId)
                .amount(100L)
                .build();

        ResponseTransferDto responseDto = ResponseTransferDto.builder()
                .amount(100L)
                .fromCard("**** **** **** 1234")
                .toCard("**** **** **** 5678")
                .balanceAfter(900L)
                .build();

        when(transactionService.transferByUser(any(RequestTransferDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100L))
                .andExpect(jsonPath("$.fromCard").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.toCard").value("**** **** **** 5678"))
                .andExpect(jsonPath("$.balanceAfter").value(900L));

        verify(transactionService).transferByUser(any(RequestTransferDto.class));
    }

    @Test
    void transfer_notEnoughBalance_throwsNotEnoughBalanceException() throws Exception {
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        RequestTransferDto request = RequestTransferDto.builder()
                .fromCard(fromCardId)
                .toCard(toCardId)
                .amount(100L)
                .build();

        when(transactionService.transferByUser(any(RequestTransferDto.class)))
                .thenThrow(new NotEnoughBalanceException("not enough balance for transaction"));

        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("not enough balance for transaction"));

        verify(transactionService).transferByUser(any(RequestTransferDto.class));
    }

    @Test
    void checkBalance_success() throws Exception {
        UUID cardId = UUID.randomUUID();

        ResponseBalanceDto responseDto = ResponseBalanceDto.builder()
                .maskedCard("**** **** **** 1234")
                .balance(1000L)
                .build();

        when(transactionService.checkBalanceByUser(cardId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/cards/{cardId}/balance", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedCard").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.balance").value(1000L));

        verify(transactionService).checkBalanceByUser(cardId);
    }
}
