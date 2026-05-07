package com.bank.frauddetection.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionGraphUpdateEvent {
    private String transactionId;
    private String fromAccountId;
    private String toAccountId;
}
