package com.example.bankcards.dto.card.create;

import com.example.bankcards.entity.CardStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ResponseCreateCardDto {
    private UUID id;
    private String last4;
    private String holderName;
    private CardStatus status;
    private long balance;
}
