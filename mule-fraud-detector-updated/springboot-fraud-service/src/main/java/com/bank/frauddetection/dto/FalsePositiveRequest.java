package com.bank.frauddetection.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class FalsePositiveRequest {
    @NotBlank
    private String referenceId;

    private String reportedBy;
    private String reason;
}
