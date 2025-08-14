package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.request.RequestBlockDto;
import com.example.bankcards.dto.card.request.ResponseBlockDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBlockRequest;
import com.example.bankcards.entity.RequestStatus;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardBlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCardService {

    private final SecurityUtil securityUtil;
    private final CardRepository cardRepository;
    private final CardBlockRequestRepository blockRequestRepository;

    @Transactional
    public ResponseBlockDto requestBlockByUser(UUID cardId, RequestBlockDto dto) {
        UUID userId = securityUtil.getCurrentUserId();

        Card card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> {
            log.warn("Block request error: card with id '{}' not found", cardId);
            return new NotFoundException("card not found");
        });

        CardBlockRequest request = CardBlockRequest.builder()
                .cardId(card.getId())
                .userId(userId)
                .reason(dto.getReason())
                .status(RequestStatus.PENDING)
                .requestedAt(Instant.now())
                .build();

        blockRequestRepository.save(request);
        log.info("Block request '{}' sent successfully for user '{}' card '{}'", request.getId(), userId, cardId);

        return ResponseBlockDto.builder()
                .requestId(request.getId())
                .maskedCard(getMasked(card.getLast4()))
                .status(request.getStatus())
                .createdAt(request.getRequestedAt())
                .build();
    }

    public String getMasked(String last4) {
        return "**** **** **** " + last4;
    }
}
