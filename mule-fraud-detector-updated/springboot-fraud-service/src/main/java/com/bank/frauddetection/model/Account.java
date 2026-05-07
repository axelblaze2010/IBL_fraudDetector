package com.bank.frauddetection.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "accounts")
public class Account {
    @Id private String id;
    @Indexed(unique = true) private String accountId;
    private BigDecimal totalReceived;
    private BigDecimal totalSent;
    private double passThroughRatio;
    private Set<String> linkedDevices;
    private LocalDateTime lastActiveDate;
    private LocalDateTime createdDate;
    private boolean hasLegitimateCredits;

    public void addDevice(String deviceHash) {
        if (linkedDevices == null) linkedDevices = new HashSet<>();
        linkedDevices.add(deviceHash);
    }
}
