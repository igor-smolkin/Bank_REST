package com.example.bankcards.dto.card.create;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestCreateCardDto {
    private String holderName;
}
