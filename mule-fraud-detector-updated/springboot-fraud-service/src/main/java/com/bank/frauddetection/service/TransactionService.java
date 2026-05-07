package com.bank.frauddetection.service;

import com.bank.frauddetection.dto.MuleCheckRequest;
import com.bank.frauddetection.event.TransactionGraphUpdateEvent;
import com.bank.frauddetection.model.Account;
import com.bank.frauddetection.model.AccountLink;
import com.bank.frauddetection.model.Transaction;
import com.bank.frauddetection.repository.AccountLinkRepository;
import com.bank.frauddetection.repository.AccountRepository;
import com.bank.frauddetection.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountLinkRepository accountLinkRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Transaction saveTransactionAndUpdateGraph(MuleCheckRequest request) {
        String deviceHash = sha256(request.getDeviceId());
        LocalDateTime now = LocalDateTime.now();

        Long processingTime = calculateForwardingTimeMinutes(request.getFromAccountId(), now);

        Transaction transaction = transactionRepository.save(Transaction.builder()
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .amount(request.getAmount())
                .currency("INR")
                .transactionType(request.getTransactionType())
                .deviceIdHash(deviceHash)
                .timestamp(now)
                .processingTime(processingTime)
                .status("COMPLETED")
                .build());

        updateAccountsAndLinks(request, deviceHash, now, processingTime);

        eventPublisher.publishEvent(TransactionGraphUpdateEvent.builder()
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .transactionId(transaction.getId())
                .build());

        return transaction;
    }

    private void updateAccountsAndLinks(
            MuleCheckRequest request,
            String deviceHash,
            LocalDateTime now,
            Long processingTime
    ) {
        Account sender = getOrCreateAccount(request.getFromAccountId());
        Account receiver = getOrCreateAccount(request.getToAccountId());

        sender.setTotalSent(sender.getTotalSent().add(request.getAmount()));
        sender.setLastActiveDate(now);
        sender.addDevice(deviceHash);

        receiver.setTotalReceived(receiver.getTotalReceived().add(request.getAmount()));
        receiver.setLastActiveDate(now);
        receiver.addDevice(deviceHash);

        updatePassThroughRatio(sender);
        updatePassThroughRatio(receiver);

        accountRepository.save(sender);
        accountRepository.save(receiver);

        upsertAccountLink(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                now,
                processingTime
        );
    }

    private Long calculateForwardingTimeMinutes(String senderAccountId, LocalDateTime currentTxnTime) {
        return transactionRepository
                .findTopByToAccountIdOrderByTimestampDesc(senderAccountId)
                .map(previousCredit -> Duration.between(previousCredit.getTimestamp(), currentTxnTime).toMinutes())
                .filter(minutes -> minutes >= 0)
                .orElse(null);
    }

    private Account getOrCreateAccount(String accountId) {
        return accountRepository.findByAccountId(accountId).orElseGet(() -> Account.builder()
                .accountId(accountId)
                .totalReceived(BigDecimal.ZERO)
                .totalSent(BigDecimal.ZERO)
                .passThroughRatio(0.0)
                .linkedDevices(new HashSet<>())
                .createdDate(LocalDateTime.now())
                .lastActiveDate(LocalDateTime.now())
                .hasLegitimateCredits(false)
                .build());
    }

    private void updatePassThroughRatio(Account account) {
        if (account.getTotalReceived().compareTo(BigDecimal.ZERO) <= 0) {
            account.setPassThroughRatio(0.0);
            return;
        }

        double ratio = account.getTotalSent()
                .divide(account.getTotalReceived(), 4, RoundingMode.HALF_UP)
                .doubleValue();

        account.setPassThroughRatio(Math.min(ratio, 1.0));
    }

    private void upsertAccountLink(
            String fromAccountId,
            String toAccountId,
            BigDecimal amount,
            LocalDateTime now,
            Long processingTime
    ) {
        AccountLink link = accountLinkRepository
                .findByFromAccountIdAndToAccountId(fromAccountId, toAccountId)
                .orElseGet(() -> AccountLink.builder()
                        .fromAccountId(fromAccountId)
                        .toAccountId(toAccountId)
                        .totalAmount(BigDecimal.ZERO)
                        .transactionCount(0)
                        .firstSeen(now)
                        .avgForwardingTime(0.0)
                        .build());

        double existingAverage = link.getAvgForwardingTime();
        long existingCount = link.getTransactionCount();

        link.setTotalAmount(link.getTotalAmount().add(amount));
        link.setTransactionCount(existingCount + 1);
        link.setLastSeen(now);

        if (processingTime != null) {
            double newAverage = ((existingAverage * existingCount) + processingTime) / (existingCount + 1);
            link.setAvgForwardingTime(newAverage);
        }

        accountLinkRepository.save(link);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(input.getBytes());
            StringBuilder hex = new StringBuilder();

            for (byte b : encoded) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash device id", e);
        }
    }
}
