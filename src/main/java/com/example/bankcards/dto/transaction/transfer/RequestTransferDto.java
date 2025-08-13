package com.example.bankcards.dto.transaction.transfer;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RequestTransferDto {
    private UUID fromCard;
    private UUID toCard;
    private Long amount;
}
