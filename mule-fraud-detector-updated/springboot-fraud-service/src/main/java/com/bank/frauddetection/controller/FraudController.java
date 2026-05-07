package com.bank.frauddetection.controller;

import com.bank.frauddetection.dto.FalsePositiveRequest;
import com.bank.frauddetection.dto.MuleCheckRequest;
import com.bank.frauddetection.dto.MuleCheckResponse;
import com.bank.frauddetection.dto.ResolveFraudCaseRequest;
import com.bank.frauddetection.model.FraudCase;
import com.bank.frauddetection.model.FraudScore;
import com.bank.frauddetection.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudDetectionService fraudDetectionService;

    @PostMapping("/check-mule")
    public MuleCheckResponse checkMule(@Valid @RequestBody MuleCheckRequest request) {
        return fraudDetectionService.checkMuleRisk(request);
    }

    @GetMapping("/risk-score/{accountId}")
    public FraudScore getLatestRiskScore(@PathVariable String accountId) {
        return fraudDetectionService.getLatestRiskScore(accountId);
    }

    @PostMapping("/report-false-positive")
    public FraudCase reportFalsePositive(@Valid @RequestBody FalsePositiveRequest request) {
        return fraudDetectionService.reportFalsePositive(request);
    }

    @GetMapping("/cases")
    public List<FraudCase> getFraudCases(@RequestParam(defaultValue = "OPEN") String status) {
        return fraudDetectionService.getFraudCases(status);
    }

    @PatchMapping("/cases/{referenceId}/resolve")
    public FraudCase resolveFraudCase(
            @PathVariable String referenceId,
            @Valid @RequestBody ResolveFraudCaseRequest request
    ) {
        return fraudDetectionService.resolveFraudCase(referenceId, request);
    }
}
