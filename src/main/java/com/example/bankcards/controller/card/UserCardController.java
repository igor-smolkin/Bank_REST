package com.example.bankcards.controller.card;

import com.example.bankcards.dto.card.request.RequestBlockDto;
import com.example.bankcards.dto.card.request.ResponseBlockDto;
import com.example.bankcards.dto.card.select.ResponseCardDto;
import com.example.bankcards.service.card.UserCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserCardController {

    private final UserCardService userCardService;

    @PostMapping("/{cardId}/block-request")
    public ResponseEntity<ResponseBlockDto> requestBlock(@PathVariable UUID cardId,
                                                         @RequestBody RequestBlockDto request) {
        ResponseBlockDto response = userCardService.requestBlock(cardId, request);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/cards")
    public Page<ResponseCardDto> getUserCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return userCardService.getAllUserCards(pageable);
    }

    @GetMapping("/cards/{cardId}")
    public ResponseEntity<ResponseCardDto> getCardById(@PathVariable UUID cardId) {
        ResponseCardDto response = userCardService.getCardById(cardId);
        return ResponseEntity.ok().body(response);
    }
}
