package com.example.bankcards.controller;

import com.example.bankcards.dto.transaction.balance.ResponseBalanceDto;
import com.example.bankcards.dto.transaction.transfer.RequestTransferDto;
import com.example.bankcards.dto.transaction.transfer.ResponseTransferDto;
import com.example.bankcards.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ResponseTransferDto> transferByUser(@RequestBody RequestTransferDto request) {
        ResponseTransferDto response = transactionService.transferByUser(request);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/cards/{cardId}/balance")
    public ResponseEntity<ResponseBalanceDto> checkBalanceByUser(@PathVariable UUID cardId) {
        ResponseBalanceDto response = transactionService.checkBalanceByUser(cardId);
        return ResponseEntity.ok().body(response);
    }
}
