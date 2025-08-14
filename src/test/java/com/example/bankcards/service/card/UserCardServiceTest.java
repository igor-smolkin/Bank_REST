package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.request.RequestBlockDto;
import com.example.bankcards.dto.card.request.ResponseBlockDto;
import com.example.bankcards.dto.card.select.ResponseCardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.RequestStatus;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserCardServiceTest {

    @Mock
    private CardMapper cardMapper;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardBlockRequestRepository blockRequestRepository;

    @InjectMocks
    private UserCardService cardService;

    @Test
    void requestBlock_success() {
        UUID cardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        RequestBlockDto dto = RequestBlockDto.builder()
                .reason("Lost card")
                .build();

        Card card = Card.builder()
                .id(cardId)
                .last4("1234")
                .userId(userId)
                .build();


        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(cardRepository.findByIdAndUserId(cardId, userId)).thenReturn(Optional.of(card));

        when(blockRequestRepository.save(any(CardBlockRequest.class)))
                .thenAnswer(invocation -> {
                    CardBlockRequest saved = invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    return saved;
                });

        ResponseBlockDto result = cardService.requestBlock(cardId, dto);

        assertNotNull(result.getRequestId());
        assertEquals("**** **** **** 1234", result.getMaskedCard());
        assertEquals(RequestStatus.PENDING, result.getStatus());
        assertNotNull(result.getCreatedAt());

        verify(blockRequestRepository).save(any(CardBlockRequest.class));
    }

    @Test
    void requestBlock_cardNotFound_throwsNotFoundException() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        RequestBlockDto dto = RequestBlockDto.builder()
                .reason("Lost card")
                .build();

        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(cardRepository.findByIdAndUserId(cardId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.requestBlock(cardId, dto));

        verify(cardRepository).findByIdAndUserId(cardId, userId);
        verifyNoInteractions(blockRequestRepository);
    }

    @Test
    void requestBlock_cardAlreadyBlocked_throwsConflictException() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        RequestBlockDto dto = RequestBlockDto.builder()
                .reason("Lost card")
                .build();

        Card blockedCard = Card.builder()
                .id(cardId)
                .userId(userId)
                .status(CardStatus.BLOCKED)
                .last4("4444")
                .build();

        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(cardRepository.findByIdAndUserId(cardId, userId)).thenReturn(Optional.of(blockedCard));

        assertThrows(ConflictException.class,
                () -> cardService.requestBlock(cardId, dto));

        verify(blockRequestRepository, never()).save(any());
    }

    @Test
    void getCardById_success() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        Card card = Card.builder()
                .id(cardId)
                .last4("1234")
                .status(CardStatus.ACTIVE)
                .userId(userId)
                .build();

        ResponseCardDto expectedDto = ResponseCardDto.builder()
                .id(cardId)
                .last4("1234")
                .status(CardStatus.ACTIVE)
                .build();

        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(cardRepository.findByIdAndUserId(cardId, userId)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(expectedDto);

        ResponseCardDto result = cardService.getCardById(cardId);

        assertThat(result).isEqualTo(expectedDto);
        verify(cardRepository).findByIdAndUserId(cardId, userId);
        verify(cardMapper).toDto(card);
    }

    @Test
    void getCardById_notFound_throwsException() {
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        when(securityUtil.getCurrentUserId()).thenReturn(userId);
        when(cardRepository.findByIdAndUserId(cardId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.getCardById(cardId));

        verify(cardRepository).findByIdAndUserId(cardId, userId);
        verifyNoInteractions(cardMapper);
    }
}
