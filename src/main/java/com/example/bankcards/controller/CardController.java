package com.example.bankcards.controller;

import com.example.bankcards.dto.card.create.RequestCreateCardDto;
import com.example.bankcards.dto.card.create.ResponseCreateCardDto;
import com.example.bankcards.dto.card.select.ResponseCardDto;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;
    private final SecurityUtil securityUtil;

    @PostMapping
    public ResponseEntity<ResponseCreateCardDto> createCard(@RequestBody RequestCreateCardDto request) {
        ResponseCreateCardDto response = cardService.createNewCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public Page<ResponseCardDto> getAllCards(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return cardService.findAllCards(page, size);
    }

    @PatchMapping("/{cardId}/block")
    public ResponseEntity<ResponseCardDto> blockCard(@PathVariable UUID cardId) {
        ResponseCardDto response = cardService.blockCard(cardId);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/{cardId}/activate")
    public ResponseEntity<ResponseCardDto> activateCard(@PathVariable UUID cardId) {
        ResponseCardDto response = cardService.activateCard(cardId);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<ResponseCardDto> getCardById(@PathVariable UUID cardId) {
        ResponseCardDto response = cardService.findCardById(cardId);
        return ResponseEntity.ok().body(response);
    }
}
