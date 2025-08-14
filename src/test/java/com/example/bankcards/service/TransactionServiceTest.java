package com.example.bankcards.service;


import com.example.bankcards.dto.transaction.balance.ResponseBalanceDto;
import com.example.bankcards.dto.transaction.transfer.RequestTransferDto;
import com.example.bankcards.dto.transaction.transfer.ResponseTransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.NotEnoughBalanceException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapper.TransactionMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void transferByUser_success() {
        UUID userId = UUID.randomUUID();
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        RequestTransferDto dto = RequestTransferDto.builder()
                .fromCard(fromCardId)
                .toCard(toCardId)
                .amount(100L)
                .build();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .cardNumber("1111222233334444")
                .balance(500L)
                .userId(userId)
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .cardNumber("5555666677778888")
                .balance(200L)
                .userId(userId)
                .build();

        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(cardRepository.findByIdAndUserId(fromCardId, userId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUserId(toCardId, userId)).thenReturn(Optional.of(toCard));

        ResponseTransferDto expectedResponse = ResponseTransferDto.builder()
                .fromCard("**** **** **** 4444")
                .toCard("**** **** **** 8888")
                .amount(dto.getAmount())
                .build();

        when(transactionMapper.toDto(any())).thenReturn(expectedResponse);

        ResponseTransferDto result = transactionService.transferByUser(dto);

        assertEquals("**** **** **** 4444", result.getFromCard());
        assertEquals("**** **** **** 8888", result.getToCard());
        assertEquals(100L, result.getAmount());

        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void transferByUser_fromCardNotFound_throwsNotFoundException() {
        UUID userId = UUID.randomUUID();
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        RequestTransferDto dto = RequestTransferDto.builder()
                .fromCard(fromCardId)
                .toCard(toCardId)
                .amount(100L)
                .build();

        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(cardRepository.findByIdAndUserId(fromCardId, userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transactionService.transferByUser(dto));

        assertEquals("sender card not found or not yours", exception.getMessage());
    }

    @Test
    void transferByUser_toCardNotFound_throwsNotFoundException() {
        UUID userId = UUID.randomUUID();
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        RequestTransferDto dto = RequestTransferDto.builder()
                .fromCard(fromCardId)
                .toCard(toCardId)
                .amount(100L)
                .build();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .cardNumber("1111222233334444")
                .balance(500L)
                .userId(userId)
                .build();

        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(cardRepository.findByIdAndUserId(fromCardId, userId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUserId(toCardId, userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transactionService.transferByUser(dto));

        assertEquals("receiver card not found or not yours", exception.getMessage());
    }

    @Test
    void transferByUser_notEnoughBalance_throwsException() {
        UUID userId = UUID.randomUUID();
        UUID fromCardId = UUID.randomUUID();
        UUID toCardId = UUID.randomUUID();

        RequestTransferDto dto = RequestTransferDto.builder()
                .fromCard(fromCardId)
                .toCard(toCardId)
                .amount(1000L)
                .build();

        Card fromCard = Card.builder()
                .id(fromCardId)
                .cardNumber("1111222233334444")
                .balance(500L)
                .userId(userId)
                .build();

        Card toCard = Card.builder()
                .id(toCardId)
                .cardNumber("5555666677778888")
                .balance(200L)
                .userId(userId)
                .build();

        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(cardRepository.findByIdAndUserId(fromCardId, userId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUserId(toCardId, userId)).thenReturn(Optional.of(toCard));

        NotEnoughBalanceException exception = assertThrows(NotEnoughBalanceException.class,
                () -> transactionService.transferByUser(dto));

        assertEquals("not enough balance for transaction", exception.getMessage());
    }

    @Test
    void checkBalanceByUser_success() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        Card card = Card.builder()
                .id(cardId)
                .cardNumber("1111222233334444")
                .last4("4444")
                .balance(500L)
                .userId(userId)
                .build();

        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(cardRepository.findByIdAndUserId(cardId, userId)).thenReturn(Optional.of(card));

        ResponseBalanceDto result = transactionService.checkBalanceByUser(cardId);

        assertEquals("**** **** **** 4444", result.getMaskedCard());
        assertEquals(500L, result.getBalance());
    }

    @Test
    void checkBalanceByUser_cardNotFound_throwsException() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(cardRepository.findByIdAndUserId(cardId, userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> transactionService.checkBalanceByUser(cardId));

        assertEquals("card not found or not yours", exception.getMessage());
    }
}
