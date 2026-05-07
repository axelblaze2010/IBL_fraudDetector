package com.bank.frauddetection.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "account_links")
@CompoundIndex(name = "from_to_idx", def = "{'fromAccountId':1,'toAccountId':1}", unique = true)
public class AccountLink {
    @Id private String id;
    @Indexed private String fromAccountId;
    @Indexed private String toAccountId;
    private BigDecimal totalAmount;
    private long transactionCount;
    private LocalDateTime firstSeen;
    private LocalDateTime lastSeen;
    private double avgForwardingTime;
}
