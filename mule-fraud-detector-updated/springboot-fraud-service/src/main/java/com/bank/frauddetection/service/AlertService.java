package com.bank.frauddetection.service;

import com.bank.frauddetection.model.FraudCase;
import com.bank.frauddetection.model.FraudScore;
import com.bank.frauddetection.repository.FraudCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final FraudCaseRepository fraudCaseRepository;

    public String createFraudCase(FraudScore fraudScore, String action) {
        String referenceId = generateReferenceId();

        FraudCase fraudCase = FraudCase.builder()
                .referenceId(referenceId)
                .accountId(fraudScore.getAccountId())
                .riskScore(fraudScore.getRiskScore())
                .action(action)
                .reasons(fraudScore.getReasons())
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .build();

        fraudCaseRepository.save(fraudCase);
        notifyOpsAsync(referenceId, fraudScore.getAccountId(), fraudScore.getRiskScore());
        return referenceId;
    }

    @Async
    public void notifyOpsAsync(String referenceId, String accountId, int riskScore) {
        // Replace this log with Slack/Email/PagerDuty integration in production.
        log.warn("CRITICAL fraud case created. referenceId={}, accountId={}, riskScore={}", referenceId, accountId, riskScore);
    }

    private String generateReferenceId() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "FRAUD-" + date + "-" + random;
    }
}
