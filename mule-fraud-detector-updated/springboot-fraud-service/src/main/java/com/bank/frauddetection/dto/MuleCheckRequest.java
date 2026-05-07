package com.bank.frauddetection.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class MuleCheckRequest {
    @NotBlank private String fromAccountId;
    @NotBlank private String toAccountId;
    @NotNull private BigDecimal amount;
    @NotBlank private String transactionType;
    @NotBlank private String deviceId;
    @NotBlank private String sessionId;
}
