package com.example.bankcards.dto.card.select;

import com.example.bankcards.entity.CardStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ResponseCardDto {
    private UUID id;
    private String cardNumber;
    private String last4;
    private String holderName;
    private Integer expiryMonth;
    private Integer expiryYear;
    private CardStatus status;
    private Long balance;
    private Instant createdAt;
    private UUID userId;
}
