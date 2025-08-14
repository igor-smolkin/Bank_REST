package com.example.bankcards.controller.card;

import com.example.bankcards.dto.card.create.RequestCreateCardDto;
import com.example.bankcards.dto.card.create.ResponseCreateCardDto;
import com.example.bankcards.dto.card.request.ResponseBlockDto;
import com.example.bankcards.dto.card.select.ResponseCardDto;
import com.example.bankcards.service.card.AdminCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminCardController {

    private final AdminCardService adminCardService;

    @PostMapping("/cards/{userId}")
    public ResponseEntity<ResponseCreateCardDto> createCard(@PathVariable UUID userId,
                                                            @RequestBody RequestCreateCardDto request) {
        ResponseCreateCardDto response = adminCardService.createNewCard(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID cardId) {
        adminCardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cards")
    public Page<ResponseCardDto> getAllCards(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return adminCardService.getAllCards(page, size);
    }

    @PatchMapping("/cards/{cardId}/block")
    public ResponseEntity<ResponseCardDto> blockCard(@PathVariable UUID cardId) {
        ResponseCardDto response = adminCardService.blockCard(cardId);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/cards/{cardId}/activate")
    public ResponseEntity<ResponseCardDto> activateCard(@PathVariable UUID cardId) {
        ResponseCardDto response = adminCardService.activateCard(cardId);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/cards/{cardId}")
    public ResponseEntity<ResponseCardDto> getCardById(@PathVariable UUID cardId) {
        ResponseCardDto response = adminCardService.findCardById(cardId);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/cards/block-requests/{requestId}/approve")
    public ResponseEntity<ResponseBlockDto> approveBlockRequest(@PathVariable UUID requestId) {
        ResponseBlockDto response = adminCardService.approveBlockRequest(requestId);
        return ResponseEntity.ok().body(response);
    }
}
