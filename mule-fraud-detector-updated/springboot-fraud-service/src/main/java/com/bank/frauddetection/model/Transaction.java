package com.bank.frauddetection.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {
    @Id private String id;
    @Indexed private String fromAccountId;
    @Indexed private String toAccountId;
    private BigDecimal amount;
    private String currency;
    private String transactionType;
    @Indexed private String deviceIdHash;
    @Indexed private LocalDateTime timestamp;
    private Long processingTime;
    private String status;
}
