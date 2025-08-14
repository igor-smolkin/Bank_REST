package com.example.bankcards.service;

import com.example.bankcards.dto.transaction.balance.ResponseBalanceDto;
import com.example.bankcards.dto.transaction.transfer.RequestTransferDto;
import com.example.bankcards.dto.transaction.transfer.ResponseTransferDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.exception.NotEnoughBalanceException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapper.TransactionMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
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
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final SecurityUtil securityUtil;
    private final TransactionMapper transactionMapper;

    @Transactional
    public ResponseTransferDto transferByUser(RequestTransferDto dto) {
        UUID userId = securityUtil.getCurrentUserId();

        Card fromCard = cardRepository.findByIdAndUserId(dto.getFromCard(), userId)
                .orElseThrow(() -> {
                    log.warn("User transfer error: sender card '{}' not found or not yours", dto.getFromCard());
                    return new NotFoundException("sender card not found or not yours");
                });

        Card toCard = cardRepository.findByIdAndUserId(dto.getToCard(), userId)
                .orElseThrow(() -> {
                    log.warn("User transfer error: Receiver card '{}' not found or not yours", dto.getToCard());
                    return new NotFoundException("receiver card not found or not yours");
                });
        return performTransfer(fromCard, toCard, dto.getAmount());
    }

    ResponseTransferDto performTransfer(Card fromCard, Card toCard, Long amount) {
        if (fromCard.getBalance() < amount) {
            log.warn("Perform transfer transaction error: not enough balance for transaction");
            throw new NotEnoughBalanceException("not enough balance for transaction");
        }

        long newBalance = fromCard.getBalance() - amount;
        fromCard.setBalance(newBalance);
        toCard.setBalance(toCard.getBalance() + amount);

        Transaction transaction = Transaction.builder()
                .status(TransactionStatus.SUCCESS)
                .amount(amount)
                .fromCard(fromCard.getId())
                .fromCardLast4(fromCard.getLast4())
                .toCard(toCard.getId())
                .toCardLast4(toCard.getLast4())
                .transactionDate(Instant.now())
                .balanceAfter(newBalance)
                .build();

        transactionRepository.save(transaction);

        log.info("Transfer '{}' -> '{}' performed successfully", getMasked(fromCard.getLast4()), getMasked(toCard.getLast4()));

        return transactionMapper.toDto(transaction);
    }

    public ResponseBalanceDto checkBalanceByUser(UUID cardId) {
        UUID userId = securityUtil.getCurrentUserId();

        Card card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> {
                    log.warn("User '{}' check balance error: card '{}' not found or not yours", userId, cardId);
                    return new NotFoundException("card not found or not yours");
                });

        return ResponseBalanceDto.builder()
                .maskedCard(getMasked(card.getLast4()))
                .balance(card.getBalance())
                .build();
    }

    public String getMasked(String last4) {
        return "**** **** **** " + last4;
    }
}
