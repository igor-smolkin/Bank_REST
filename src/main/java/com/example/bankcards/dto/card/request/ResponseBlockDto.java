package com.example.bankcards.dto.card.request;

import com.example.bankcards.entity.RequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ResponseBlockDto {
    private UUID requestId;
    private String maskedCard;
    private RequestStatus status;
    private Instant createdAt;
}
