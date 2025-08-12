package com.example.bankcards.mapper;

import com.example.bankcards.dto.card.select.ResponseCardDto;
import com.example.bankcards.entity.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {
    public ResponseCardDto toDto(Card card) {
        return ResponseCardDto.builder()
                .id(card.getId())
                .cardNumber("**** **** **** " + card.getLast4())
                .last4(card.getLast4())
                .holderName(card.getHolderName())
                .expiryMonth(card.getExpiryMonth())
                .expiryYear(card.getExpiryYear())
                .status(card.getStatus())
                .balance(card.getBalance())
                .createdAt(card.getCreatedAt())
                .userId(card.getUserId())
                .build();
    }
}
