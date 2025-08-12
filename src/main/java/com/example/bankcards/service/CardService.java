package com.example.bankcards.service;

import com.example.bankcards.dto.card.create.RequestCreateCardDto;
import com.example.bankcards.dto.card.create.ResponseCreateCardDto;
import com.example.bankcards.dto.card.select.ResponseCardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_RETRIES = 5;
    private final int CARD_VALIDITY_YEARS = 3;

    private final CardRepository cardRepository;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseCreateCardDto createNewCard(RequestCreateCardDto dto, UUID userId) {
        int attempts = 0;
        String email = securityUtil.getCurrentUsername();
        log.info("Создание карты администратором '{}'", email);
        while (true) {
            attempts++;
            String cardNumber = generateCardNumber();
            String last4 = cardNumber.substring(cardNumber.length() - 4);

            Card card = Card.builder()
                    .cardNumber(cardNumber)
                    .last4(last4)
                    .holderName(dto.getHolderName())
                    .expiryMonth(getExpiryMonth())
                    .expiryYear(getExpiryYear())
                    .status(CardStatus.ACTIVE)
                    .balance(0)
                    .createdAt(Instant.now())
                    .userId(userId)
                    .build();
            try {
                cardRepository.save(card);
                log.info("card created"); // TODO - нормальные логи
                return ResponseCreateCardDto.builder()
                        .id(card.getId())
                        .last4(getMasked(card.getLast4()))
                        .holderName(card.getHolderName())
                        .status(card.getStatus())
                        .balance(card.getBalance())
                        .build();
            } catch (DataIntegrityViolationException ex) {
                log.warn("Попытка {}: дублирование номера карты, пробуем снова", attempts);
                if (attempts >= MAX_RETRIES) {
                    log.error("Не удалось сгенерировать уникальный номер карты после {} попыток", MAX_RETRIES);
                    throw new RuntimeException("Ошибка генерации уникального номера карты", ex);
                }
            }
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteCard(UUID cardId) {
        log.info("deleting card '{}'", cardId);
        String email = securityUtil.getCurrentUsername();
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.warn("Delete error: card with id '{}' not found", cardId);
                    return new NotFoundException("card not found");
                });
        cardRepository.delete(card);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<ResponseCardDto> findAllCards(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return cardRepository.findAll(pageable)
                .map(cardMapper::toDto);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseCardDto blockCard(UUID cardId) {
        String email = securityUtil.getCurrentUsername();
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.warn("Blocking error: card with id '{}' not found", cardId);
                    return new NotFoundException("card not found");
                });

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new ConflictException("Card already blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

        log.info("Admin '{}' blocked card '{}'", email, cardId);
        return cardMapper.toDto(card);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseCardDto activateCard(UUID cardId) {
        String email = securityUtil.getCurrentUsername();
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.warn("Activating error: card with id '{}' not found", cardId);
                    return new NotFoundException("card not found");
                });

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new ConflictException("Card already activated");
        }

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);

        log.info("Admin '{}' activated card '{}'", email, cardId);
        return cardMapper.toDto(card);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseCardDto findCardById(UUID cardId) {
        String email = securityUtil.getCurrentUsername();
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.warn("Searching error: card with id '{}' not found", cardId);
                    return new NotFoundException("card not found");
                });
        return cardMapper.toDto(card);
    }

    public String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    public int getExpiryMonth() {
        return YearMonth.now().plusYears(CARD_VALIDITY_YEARS).getMonthValue();
    }

    public int getExpiryYear() {
        return YearMonth.now().plusYears(CARD_VALIDITY_YEARS).getYear() % 100;
    }

    public String getMasked(String last4) {
        return "**** **** **** " + last4;
    }
}
