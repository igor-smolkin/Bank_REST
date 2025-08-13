package com.example.bankcards.dto.transaction.transfer;

import com.example.bankcards.entity.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ResponseTransferDto {
    private UUID id;
    private TransactionStatus status;
    private Long amount;
    private String fromCard;
    private String toCard;
    private Instant transactionDate;
    private Long balanceAfter;
}
