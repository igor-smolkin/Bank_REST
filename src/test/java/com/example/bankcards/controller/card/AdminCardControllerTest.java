package com.example.bankcards.controller.card;

import com.example.bankcards.dto.card.create.RequestCreateCardDto;
import com.example.bankcards.dto.card.create.ResponseCreateCardDto;
import com.example.bankcards.dto.card.request.ResponseBlockDto;
import com.example.bankcards.dto.card.select.ResponseCardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.RequestStatus;
import com.example.bankcards.exception.CardNumberGenerationException;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.security.config.JwtAuthenticationEntryPoint;
import com.example.bankcards.security.filter.JwtAuthenticationFilter;
import com.example.bankcards.security.service.CustomUserDetailsService;
import com.example.bankcards.security.service.JwtService;
import com.example.bankcards.service.card.AdminCardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCardController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminCardControllerTest {

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
    private AdminCardService adminCardService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void createCard_success_returnsCreatedCard() throws Exception {

        RequestCreateCardDto request = RequestCreateCardDto.builder()
                .holderName("John Doe")
                .build();

        ResponseCreateCardDto response = ResponseCreateCardDto.builder()
                .id(UUID.randomUUID())
                .last4("****1234")
                .holderName("John Doe")
                .status(CardStatus.ACTIVE)
                .balance(0)
                .build();

        when(adminCardService.createNewCard(eq(userId), any(RequestCreateCardDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/admin/cards/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId().toString()))
                .andExpect(jsonPath("$.last4").value(response.getLast4()))
                .andExpect(jsonPath("$.holderName").value(response.getHolderName()))
                .andExpect(jsonPath("$.status").value(response.getStatus().toString()))
                .andExpect(jsonPath("$.balance").value(response.getBalance()));
    }

    @Test
    void createCard_generationCardNumberError_throwsCardNumberGenerationException() throws Exception {

        RequestCreateCardDto request = RequestCreateCardDto.builder()
                .holderName("John Doe")
                .build();

        when(adminCardService.createNewCard(eq(userId), any(RequestCreateCardDto.class)))
                .thenThrow(new CardNumberGenerationException("Card number generation error"));

        mockMvc.perform(post("/api/admin/cards/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCard_success_returnsNoContent() throws Exception {

        UUID cardId = UUID.randomUUID();
        doNothing().when(adminCardService).deleteCard(cardId);

        mockMvc.perform(delete("/api/admin/cards/{cardId}", cardId))
                .andExpect(status().isNoContent());

        verify(adminCardService).deleteCard(cardId);
    }

    @Test
    void deleteCard_cardNotFound_returnsNotFound() throws Exception {

        UUID cardId = UUID.randomUUID();

        doThrow(new NotFoundException("card not found"))
                .when(adminCardService).deleteCard(cardId);

        mockMvc.perform(delete("/api/admin/cards/{cardId}", cardId))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(result -> assertEquals("card not found", result.getResolvedException().getMessage()));

        verify(adminCardService).deleteCard(cardId);
    }

    @Test
    void getAllCards_returnsPagedCards() throws Exception {

        ResponseCardDto cardDto = ResponseCardDto.builder()
                .id(UUID.randomUUID())
                .holderName("John Doe")
                .last4("****1234")
                .status(CardStatus.ACTIVE)
                .balance(100L)
                .build();

        Page<ResponseCardDto> page = new PageImpl<>(List.of(cardDto));

        when(adminCardService.getAllCards(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/admin/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].holderName").value("John Doe"))
                .andExpect(jsonPath("$.content[0].last4").value("****1234"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.content[0].balance").value(100));

        verify(adminCardService).getAllCards(0, 10);
    }

    @Test
    void blockCard_success_returnsBlockedCard() throws Exception {

        UUID cardId = UUID.randomUUID();
        ResponseCardDto blockedCardDto = ResponseCardDto.builder()
                .id(cardId)
                .holderName("John Doe")
                .status(CardStatus.BLOCKED)
                .build();

        when(adminCardService.blockCard(cardId)).thenReturn(blockedCardDto);

        mockMvc.perform(patch("/api/admin/cards/{cardId}/block", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.holderName").value("John Doe"));

        verify(adminCardService).blockCard(cardId);
    }

    @Test
    void blockCard_cardAlreadyBlocked_throwsConflictException() throws Exception {

        UUID cardId = UUID.randomUUID();

        when(adminCardService.blockCard(cardId))
                .thenThrow(new ConflictException("Card already blocked"));

        mockMvc.perform(patch("/api/admin/cards/{cardId}/block", cardId))
                .andExpect(status().isConflict());
    }

    @Test
    void blockCard_cardNotFound_throwsNotFoundException() throws Exception {

        UUID cardId = UUID.randomUUID();

        when(adminCardService.blockCard(cardId))
                .thenThrow(new NotFoundException("card not found"));

        mockMvc.perform(patch("/api/admin/cards/{cardId}/block", cardId))
                .andExpect(status().isNotFound());
    }

    @Test
    void activateCard_success_returnsActivatedCard() throws Exception {

        UUID cardId = UUID.randomUUID();
        ResponseCardDto activatedCardDto = ResponseCardDto.builder()
                .id(cardId)
                .holderName("John Doe")
                .status(CardStatus.ACTIVE)
                .build();

        when(adminCardService.activateCard(cardId)).thenReturn(activatedCardDto);

        mockMvc.perform(patch("/api/admin/cards/{cardId}/activate", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.holderName").value("John Doe"));

        verify(adminCardService).activateCard(cardId);
    }

    @Test
    void activateCard_cardAlreadyActivated_throwsConflictException() throws Exception {

        UUID cardId = UUID.randomUUID();

        when(adminCardService.activateCard(cardId))
                .thenThrow(new ConflictException("Card already activated"));

        mockMvc.perform(patch("/api/admin/cards/{cardId}/activate", cardId))
                .andExpect(status().isConflict());
    }

    @Test
    void activateCard_cardNotFound_throwsNotFoundException() throws Exception {

        UUID cardId = UUID.randomUUID();

        when(adminCardService.activateCard(cardId))
                .thenThrow(new NotFoundException("card not found"));

        mockMvc.perform(patch("/api/admin/cards/{cardId}/activate", cardId))
                .andExpect(status().isNotFound());
    }

    @Test
    void findCardById_success_returnsCard() throws Exception {

        UUID cardId = UUID.randomUUID();

        ResponseCardDto cardDto = ResponseCardDto.builder()
                .id(cardId)
                .holderName("John Doe")
                .status(CardStatus.ACTIVE)
                .build();

        when(adminCardService.findCardById(cardId)).thenReturn(cardDto);

        mockMvc.perform(get("/api/admin/cards/{cardId}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId.toString()))
                .andExpect(jsonPath("$.holderName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(adminCardService).findCardById(cardId);
    }

    @Test
    void findCardById_notFound_throwsNotFoundException() throws Exception {

        UUID cardId = UUID.randomUUID();

        when(adminCardService.findCardById(cardId))
                .thenThrow(new NotFoundException("card not found"));

        mockMvc.perform(get("/api/admin/cards/{cardId}", cardId))
                .andExpect(status().isNotFound());

        verify(adminCardService).findCardById(cardId);
    }

    @Test
    void approveBlockRequest_success_returnsBlockedCard() throws Exception {

        UUID requestId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        CardBlockRequest request = CardBlockRequest.builder()
                .id(requestId)
                .cardId(cardId)
                .status(RequestStatus.PENDING)
                .requestedAt(Instant.now())
                .build();

        Card card = Card.builder()
                .id(cardId)
                .holderName("John Doe")
                .status(CardStatus.ACTIVE)
                .last4("1234")
                .build();

        ResponseBlockDto responseDto = ResponseBlockDto.builder()
                .requestId(requestId)
                .maskedCard("**** **** **** 1234")
                .status(RequestStatus.APPROVED)
                .createdAt(request.getRequestedAt())
                .build();

        when(adminCardService.approveBlockRequest(requestId)).thenReturn(responseDto);

        mockMvc.perform(patch("/api/admin/cards/block-requests/{requestId}/approve", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId.toString()))
                .andExpect(jsonPath("$.maskedCard").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(adminCardService).approveBlockRequest(requestId);
    }

    @Test
    void approveBlockRequest_notFound_throwsNotFoundException() throws Exception {

        UUID requestId = UUID.randomUUID();

        when(adminCardService.approveBlockRequest(requestId))
                .thenThrow(new NotFoundException("request not found"));

        mockMvc.perform(patch("/api/admin/cards/block-requests/{requestId}/approve", requestId))
                .andExpect(status().isNotFound());

        verify(adminCardService).approveBlockRequest(requestId);
    }

    @Test
    void approveBlockRequest_alreadyProcessed_throwsConflictException() throws Exception {

        UUID requestId = UUID.randomUUID();

        when(adminCardService.approveBlockRequest(requestId))
                .thenThrow(new ConflictException("request already processed"));

        mockMvc.perform(patch("/api/admin/cards/block-requests/{requestId}/approve", requestId))
                .andExpect(status().isConflict());

        verify(adminCardService).approveBlockRequest(requestId);
    }
}
