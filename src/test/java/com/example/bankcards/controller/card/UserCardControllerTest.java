package com.example.bankcards.controller.card;

import com.example.bankcards.dto.card.request.RequestBlockDto;
import com.example.bankcards.dto.card.request.ResponseBlockDto;
import com.example.bankcards.dto.card.select.ResponseCardDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.RequestStatus;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.security.config.JwtAuthenticationEntryPoint;
import com.example.bankcards.security.filter.JwtAuthenticationFilter;
import com.example.bankcards.security.service.CustomUserDetailsService;
import com.example.bankcards.security.service.JwtService;
import com.example.bankcards.service.card.UserCardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserCardController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserCardControllerTest {

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
    private UserCardService userCardService;

    @Test
    void requestBlock_success() throws Exception {
        UUID cardId = UUID.randomUUID();

        RequestBlockDto request = RequestBlockDto.builder()
                .reason("Lost card")
                .build();

        ResponseBlockDto response = ResponseBlockDto.builder()
                .requestId(UUID.randomUUID())
                .maskedCard("**** **** **** 1234")
                .status(RequestStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        when(userCardService.requestBlock(eq(cardId), any(RequestBlockDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/{cardId}/block-request", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedCard").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(userCardService).requestBlock(eq(cardId), any(RequestBlockDto.class));
    }

    @Test
    void requestBlock_alreadyBlocked_throwsConflictException() throws Exception {
        UUID cardId = UUID.randomUUID();

        RequestBlockDto request = RequestBlockDto.builder()
                .reason("Lost card")
                .build();

        when(userCardService.requestBlock(eq(cardId), any(RequestBlockDto.class)))
                .thenThrow(new ConflictException("Card already blocked"));

        mockMvc.perform(post("/api/{cardId}/block-request", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Card already blocked"));

        verify(userCardService).requestBlock(eq(cardId), any(RequestBlockDto.class));
    }

    @Test
    void getUserCards_success() throws Exception {
        ResponseCardDto cardDto = ResponseCardDto.builder()
                .id(UUID.randomUUID())
                .cardNumber("**** **** **** 1234")
                .balance(1000L)
                .status(CardStatus.ACTIVE)
                .build();

        Page<ResponseCardDto> page = new PageImpl<>(List.of(cardDto));
        when(userCardService.getAllUserCards(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.content[0].balance").value(1000));

        verify(userCardService).getAllUserCards(any(Pageable.class));
    }

    @Test
    void getCardById_success() throws Exception {
        UUID cardId = UUID.randomUUID();

        ResponseCardDto response = ResponseCardDto.builder()
                .id(cardId)
                .cardNumber("**** **** **** 1234")
                .balance(1000L)
                .status(CardStatus.ACTIVE)
                .build();

        when(userCardService.getCardById(cardId)).thenReturn(response);

        mockMvc.perform(get("/api/cards/{cardId}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.balance").value(1000));

        verify(userCardService).getCardById(cardId);
    }
}
