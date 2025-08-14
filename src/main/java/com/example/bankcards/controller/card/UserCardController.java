package com.example.bankcards.controller.card;

import com.example.bankcards.dto.card.request.RequestBlockDto;
import com.example.bankcards.dto.card.request.ResponseBlockDto;
import com.example.bankcards.service.card.UserCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
public class UserCardController {

    private final UserCardService userCardService;

    @PostMapping("/{cardId}/block-request")
    public ResponseEntity<ResponseBlockDto> requestBlockByUser(@PathVariable UUID cardId,
                                                               @RequestBody RequestBlockDto request) {
        ResponseBlockDto response = userCardService.requestBlockByUser(cardId, request);
        return ResponseEntity.ok().body(response);
    }
}
