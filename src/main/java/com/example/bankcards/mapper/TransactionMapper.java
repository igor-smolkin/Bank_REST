package com.example.bankcards.mapper;

import com.example.bankcards.dto.transaction.transfer.ResponseTransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionMapper {

    public ResponseTransferDto toDto(Transaction transaction) {
        return ResponseTransferDto.builder()
                .id(transaction.getId())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .fromCard(maskCard(transaction.getFromCardLast4()))
                .toCard(maskCard(transaction.getToCardLast4()))
                .transactionDate(transaction.getTransactionDate())
                .balanceAfter(transaction.getBalanceAfter())
                .build();
    }

    private String maskCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + last4;
    }
}
