package com.bank.frauddetection.service;

import com.bank.frauddetection.dto.MlRiskRequest;
import com.bank.frauddetection.dto.MlRiskResponse;
import com.bank.frauddetection.model.Account;
import com.bank.frauddetection.model.AccountLink;
import com.bank.frauddetection.repository.AccountLinkRepository;
import com.bank.frauddetection.repository.AccountRepository;
import com.bank.frauddetection.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MlRiskService {

    private final WebClient.Builder webClientBuilder;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountLinkRepository accountLinkRepository;
    private final MuleGraphService muleGraphService;

    @Value("${ml.service.base-url}")
    private String mlBaseUrl;

    @Value("${ml.service.predict-path}")
    private String predictPath;

    public MlRiskResponse getMlRiskScore(String accountId) {
        try {
            return webClientBuilder.build()
                    .post()
                    .uri(mlBaseUrl + predictPath)
                    .bodyValue(buildMlRequest(accountId))
                    .retrieve()
                    .bodyToMono(MlRiskResponse.class)
                    .timeout(Duration.ofMillis(700))
                    .block();
        } catch (Exception e) {
            MlRiskResponse fallback = new MlRiskResponse();
            fallback.setMlScore(0);
            fallback.setFraudProbability(0.0);
            fallback.setMlReasons(List.of("ML service unavailable, fallback score applied"));
            return fallback;
        }
    }

    private MlRiskRequest buildMlRequest(String accountId) {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        LocalDateTime now = LocalDateTime.now();

        long txnCountLast1Hour = transactionRepository.countByToAccountIdAndTimestampAfter(accountId, now.minusHours(1));
        long txnCountLast24Hours = transactionRepository.countByToAccountIdAndTimestampAfter(accountId, now.minusHours(24));

        BigDecimal received24h = transactionRepository.findByToAccountIdAndTimestampAfter(accountId, now.minusHours(24))
                .stream()
                .map(txn -> txn.getAmount() == null ? BigDecimal.ZERO : txn.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sent24h = transactionRepository.findByFromAccountIdAndTimestampAfter(accountId, now.minusHours(24))
                .stream()
                .map(txn -> txn.getAmount() == null ? BigDecimal.ZERO : txn.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int linkedDeviceCount = account.getLinkedDevices() == null ? 0 : account.getLinkedDevices().size();
        long accountAgeDays = account.getCreatedDate() == null ? 0 : Duration.between(account.getCreatedDate(), now).toDays();
        long inactiveDays = account.getLastActiveDate() == null ? 0 : Duration.between(account.getLastActiveDate(), now).toDays();

        double avgForwardingTime = accountLinkRepository.findByFromAccountId(accountId)
                .stream()
                .mapToDouble(AccountLink::getAvgForwardingTime)
                .filter(minutes -> minutes > 0)
                .average()
                .orElse(0.0);

        return MlRiskRequest.builder()
                .txnCountLast1Hour(txnCountLast1Hour)
                .txnCountLast24Hours(txnCountLast24Hours)
                .amountReceivedLast24Hours(received24h)
                .amountSentLast24Hours(sent24h)
                .passThroughRatio(account.getPassThroughRatio())
                .avgForwardingTime(avgForwardingTime)
                .uniqueSendersCount((int) accountLinkRepository.countByToAccountId(accountId))
                .uniqueReceiversCount((int) accountLinkRepository.countByFromAccountId(accountId))
                .linkedDeviceCount(linkedDeviceCount)
                .accountAgeDays(accountAgeDays)
                .inactiveDaysBeforeTxn(inactiveDays)
                .hopDistanceFromFraudNode(muleGraphService.distanceFromKnownFraudNode(accountId))
                .circularFlowDetected(muleGraphService.hasCircularFlow(accountId))
                .fanOutCount(muleGraphService.getFanOutCount(accountId))
                .hasLegitimateCredits(account.isHasLegitimateCredits())
                .build();
    }
}
