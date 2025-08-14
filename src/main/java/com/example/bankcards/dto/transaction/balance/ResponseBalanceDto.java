package com.example.bankcards.dto.transaction.balance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseBalanceDto {
    private String maskedCard;
    private Long balance;
}
