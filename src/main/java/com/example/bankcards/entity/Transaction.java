package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "from_card", nullable = false)
    private UUID fromCard;

    @Column(name = "from_card_last4", nullable = false)
    private String fromCardLast4;

    @Column(name = "to_card", nullable = false)
    private UUID toCard;

    @Column(name = "to_card_last4", nullable = false)
    private String toCardLast4;

    @Column(name = "transaction_date", nullable = false)
    private Instant transactionDate;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;
}
