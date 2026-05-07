package com.bank.frauddetection.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ResolveFraudCaseRequest {
    @NotBlank
    private String resolvedBy;

    private String resolutionNote;
}
