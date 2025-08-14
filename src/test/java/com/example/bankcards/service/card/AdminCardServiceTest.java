package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.create.RequestCreateCardDto;
import com.example.bankcards.dto.card.create.ResponseCreateCardDto;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminCardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CardBlockRequestRepository cardBlockRepository;

    @InjectMocks
    private AdminCardService cardService;

    @Test
    void createNewCard_success() {
        UUID userId = UUID.randomUUID();
        RequestCreateCardDto dto = RequestCreateCardDto.builder()
                .holderName("John Doe")
                .build();

        when(securityUtil.getCurrentUsername()).thenReturn("admin@example.com");

        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(UUID.fromString("0e0fac3d-7137-41c7-b596-d83b2bfc1f4a"));
            card.setLast4("4444");
            return card;
        });

        ResponseCreateCardDto response = cardService.createNewCard(userId, dto);

        assertNotNull(response.getId());
        assertEquals(UUID.fromString("0e0fac3d-7137-41c7-b596-d83b2bfc1f4a"), response.getId());
        assertEquals("**** **** **** 4444", response.getLast4());
        assertEquals("John Doe", response.getHolderName());
        assertEquals(CardStatus.ACTIVE, response.getStatus());
        assertEquals(0, response.getBalance());
    }

    @Test
    void createNewCard_retryOnDuplicateCardNumber_success() {
        UUID userId = UUID.randomUUID();
        RequestCreateCardDto dto = RequestCreateCardDto.builder()
                .holderName("John Doe")
                .build();

        when(securityUtil.getCurrentUsername()).thenReturn("admin@example.com");

        when(cardRepository.save(any(Card.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"))
                .thenAnswer(invocation -> {
                    Card card = invocation.getArgument(0);
                    card.setId(UUID.randomUUID());
                    card.setLast4("4444");
                    return card;
                });

        ResponseCreateCardDto response = cardService.createNewCard(userId, dto);

        assertNotNull(response.getId());
        assertEquals("**** **** **** 4444", response.getLast4());
        assertEquals("John Doe", response.getHolderName());
    }

    @Test
    void createNewCard_retryFails_throwsException() {
        UUID userId = UUID.randomUUID();
        RequestCreateCardDto dto = RequestCreateCardDto.builder()
                .holderName("John Doe")
                .build();

        when(securityUtil.getCurrentUsername()).thenReturn("admin@example.com");

        when(cardRepository.save(any(Card.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cardService.createNewCard(userId, dto));

        assertTrue(exception.getMessage().contains("Card number generation error"));
    }

    @Test
    void deleteCard_success() {
        UUID cardId = UUID.randomUUID();
        Card card = Card.builder().id(cardId).build();

        when(securityUtil.getCurrentUsername()).thenReturn("admin@example.com");
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.deleteCard(cardId);

        verify(cardRepository).delete(card);
    }

    @Test
    void deleteCard_cardNotFound_throwsNotFoundException() {
        UUID cardId = UUID.randomUUID();

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> cardService.deleteCard(cardId));

        assertEquals("card not found", exception.getMessage());
    }

    @Test
    void getAllCards_success() {
        int page = 0;
        int size = 2;

        Card card1 = Card.builder().id(UUID.randomUUID()).last4("1234").build();
        Card card2 = Card.builder().id(UUID.randomUUID()).last4("5678").build();

        List<Card> cards = List.of(card1, card2);
        Page<Card> cardPage = new PageImpl<>(cards);

        when(cardRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())))
                .thenReturn(cardPage);
        when(cardMapper.toDto(any(Card.class)))
                .thenAnswer(invocation -> {
                    Card card = invocation.getArgument(0);
                    return ResponseCardDto.builder()
                            .id(card.getId())
                            .last4(card.getLast4())
                            .build();
                });

        Page<ResponseCardDto> result = cardService.getAllCards(page, size);

        assertEquals(2, result.getContent().size());
        assertEquals(card1.getId(), result.getContent().get(0).getId());
        assertEquals(card2.getId(), result.getContent().get(1).getId());
    }

    @Test
    void getAllCards_emptyPage() {
        int page = 0;
        int size = 2;

        Page<Card> cardPage = new PageImpl<>(Collections.emptyList());

        when(cardRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())))
                .thenReturn(cardPage);

        Page<ResponseCardDto> result = cardService.getAllCards(page, size);

        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void blockCard_success() {
        UUID cardId = UUID.randomUUID();
        Card card = Card.builder()
                .id(cardId)
                .status(CardStatus.ACTIVE)
                .build();

        when(securityUtil.getCurrentUsername()).thenReturn("admin@example.com");
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardMapper.toDto(any(Card.class))).thenAnswer(invocation -> {
            Card c = invocation.getArgument(0);
            return ResponseCardDto.builder()
                    .id(c.getId())
                    .status(c.getStatus())
                    .build();
        });

        ResponseCardDto result = cardService.blockCard(cardId);

        assertEquals(CardStatus.BLOCKED, result.getStatus());
        assertEquals(cardId, result.getId());
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_notFound_throwsNotFoundException() {
        UUID cardId = UUID.randomUUID();

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> cardService.blockCard(cardId));

        assertEquals("card not found", ex.getMessage());
    }

    @Test
    void blockCard_alreadyBlocked_throwsConflictException() {
        UUID cardId = UUID.randomUUID();
        Card card = Card.builder()
                .id(cardId)
                .status(CardStatus.BLOCKED)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> cardService.blockCard(cardId));

        assertEquals("Card already blocked", ex.getMessage());
    }

    @Test
    void activateCard_success() {
        UUID cardId = UUID.randomUUID();
        Card card = Card.builder()
                .id(cardId)
                .status(CardStatus.BLOCKED)
                .build();

        when(securityUtil.getCurrentUsername()).thenReturn("admin@example.com");
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardMapper.toDto(any(Card.class))).thenAnswer(invocation -> {
            Card c = invocation.getArgument(0);
            return ResponseCardDto.builder()
                    .id(c.getId())
                    .status(c.getStatus())
                    .build();
        });

        ResponseCardDto result = cardService.activateCard(cardId);

        assertEquals(CardStatus.ACTIVE, result.getStatus());
        assertEquals(cardId, result.getId());
        verify(cardRepository).save(card);
    }

    @Test
    void throwsNotFoundException_throwsNotFoundException() {
        UUID cardId = UUID.randomUUID();

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> cardService.activateCard(cardId));

        assertEquals("card not found", ex.getMessage());
    }

    @Test
    void activateCard_alreadyActive_throwsConflictException() {
        UUID cardId = UUID.randomUUID();
        Card card = Card.builder()
                .id(cardId)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> cardService.activateCard(cardId));

        assertEquals("Card already activated", ex.getMessage());
    }

    @Test
    void findCardById_success() {
        UUID cardId = UUID.randomUUID();
        Card card = Card.builder()
                .id(cardId)
                .status(CardStatus.ACTIVE)
                .build();

        when(securityUtil.getCurrentUsername()).thenReturn("admin@example.com");
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(ResponseCardDto.builder()
                .id(cardId)
                .status(CardStatus.ACTIVE)
                .build());

        ResponseCardDto result = cardService.findCardById(cardId);

        assertEquals(cardId, result.getId());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
    }

    @Test
    void findCardById_notFound_throwsNotFoundException() {
        UUID cardId = UUID.randomUUID();

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> cardService.findCardById(cardId));

        assertEquals("card not found", ex.getMessage());
    }

    @Test
    void approveBlockRequest_success() {
        UUID requestId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        CardBlockRequest request = CardBlockRequest.builder()
                .id(requestId)
                .cardId(cardId)
                .status(RequestStatus.PENDING)
                .requestedAt(Instant.parse("2025-08-14T10:00:00Z"))
                .build();

        Card card = Card.builder()
                .id(cardId)
                .last4("4444")
                .status(CardStatus.ACTIVE)
                .build();

        when(securityUtil.getCurrentUsername()).thenReturn("admin@example.com");
        when(cardBlockRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        ResponseBlockDto result = cardService.approveBlockRequest(requestId);

        assertEquals(requestId, result.getRequestId());
        assertEquals("**** **** **** 4444", result.getMaskedCard());
        assertEquals(RequestStatus.APPROVED, result.getStatus());
        assertEquals(request.getRequestedAt(), result.getCreatedAt());

        verify(cardRepository).save(card);
        verify(cardBlockRepository).save(request);
    }

    @Test
    void approveBlockRequest_requestNotFound_throwsNotFoundException() {
        UUID requestId = UUID.randomUUID();

        when(cardBlockRepository.findById(requestId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> cardService.approveBlockRequest(requestId));

        assertEquals("request not found", ex.getMessage());
    }

    @Test
    void approveBlockRequest_alreadyProcessed_throwsConflictException() {
        UUID requestId = UUID.randomUUID();

        CardBlockRequest request = CardBlockRequest.builder()
                .id(requestId)
                .status(RequestStatus.APPROVED)
                .build();

        when(cardBlockRepository.findById(requestId)).thenReturn(Optional.of(request));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> cardService.approveBlockRequest(requestId));

        assertEquals("request already processed", ex.getMessage());
    }

    @Test
    void approveBlockRequest_cardNotFound_throwsNotFoundException() {
        UUID requestId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        CardBlockRequest request = CardBlockRequest.builder()
                .id(requestId)
                .cardId(cardId)
                .status(RequestStatus.PENDING)
                .build();

        when(cardBlockRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> cardService.approveBlockRequest(requestId));

        assertEquals("card not found", ex.getMessage());
    }
}
