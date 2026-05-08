package com.bank.frauddetection.service;

import com.bank.frauddetection.model.FraudCase;
import com.bank.frauddetection.repository.AccountLinkRepository;
import com.bank.frauddetection.repository.FraudCaseRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MuleGraphService {

    private static final int MAX_DEPTH = 5;

    private final MongoTemplate mongoTemplate;
    private final AccountLinkRepository accountLinkRepository;
    private final FraudCaseRepository fraudCaseRepository;

    public boolean hasCircularFlow(String accountId) {
        List<Document> pipeline = Arrays.asList(
                new Document("$match", new Document("fromAccountId", accountId)),
                new Document("$graphLookup", new Document("from", "account_links")
                        .append("startWith", "$toAccountId")
                        .append("connectFromField", "toAccountId")
                        .append("connectToField", "fromAccountId")
                        .append("as", "path")
                        .append("maxDepth", MAX_DEPTH)
                        .append("depthField", "hopCount")),
                new Document("$match", new Document("path.toAccountId", accountId))
        );

        return mongoTemplate.getCollection("account_links")
                .aggregate(pipeline)
                .iterator()
                .hasNext();
    }

    /**
     * Detects fan-out with circular flow pattern:
     * Account1 → Account2 → (ACC3, ACC4, ACC5 split/fan-out) → back to Account1
     *
     * This is a money laundering technique where funds:
     * 1. Enter through an account
     * 2. Get split to multiple recipients (fan-out)
     * 3. Return back to the original account (circular)
     *
     * @param accountId The account to check
     * @return true if fund distribution followed by convergence back is detected
     */
    public boolean hasFanOutWithCircularFlow(String accountId) {
        // Step 1: Check if account has incoming transactions (receiver)
        long incomingCount = accountLinkRepository.countByToAccountId(accountId);
        if (incomingCount == 0) {
            return false;  // No incoming transactions, can't have circular return
        }

        // Step 2: Check if account sends to multiple recipients (fan-out)
        int fanOutCount = getFanOutCount(accountId);
        if (fanOutCount < 2) {
            return false;  // Not enough recipients for fan-out pattern
        }

        // Step 3: Check for circular flow - money comes back
        List<Document> circularPipeline = Arrays.asList(
                new Document("$match", new Document("fromAccountId", accountId)),
                new Document("$graphLookup", new Document("from", "account_links")
                        .append("startWith", "$toAccountId")
                        .append("connectFromField", "toAccountId")
                        .append("connectToField", "fromAccountId")
                        .append("as", "returnPath")
                        .append("maxDepth", MAX_DEPTH)
                        .append("depthField", "hopCount")),
                new Document("$match", new Document("returnPath.toAccountId", accountId))
        );

        return mongoTemplate.getCollection("account_links")
                .aggregate(circularPipeline)
                .iterator()
                .hasNext();
    }

    /**
     * Detects multi-level fan-out: account sends through intermediary to multiple final recipients
     * Pattern: Account1 → IntermediaryAcc → (ACC3, ACC4, ACC5 fan-out)
     *
     * @param accountId The account to check
     * @return true if multi-level fan-out is detected (2+ hops with fan-out at final level)
     */
    public boolean hasMultiLevelFanOut(String accountId) {
        // Get direct recipients
        List<String> directRecipients = accountLinkRepository.findByFromAccountId(accountId)
                .stream()
                .map(link -> link.getToAccountId())
                .toList();

        if (directRecipients.isEmpty()) {
            return false;
        }

        // For each direct recipient, check if they fan-out significantly
        for (String intermediary : directRecipients) {
            int fanOutFromIntermediary = (int) accountLinkRepository
                    .countByFromAccountIdAndLastSeenAfter(intermediary, LocalDateTime.now().minusMinutes(30));

            // If intermediary sends to 3+ accounts, it's a fan-out pattern
            if (fanOutFromIntermediary >= 3) {
                return true;
            }
        }

        return false;
    }

    public int getFanOutCount(String accountId) {
        return getFanOutCount(accountId, LocalDateTime.now().minusMinutes(10));
    }

    public int getFanOutCount(String accountId, LocalDateTime after) {
        return (int) accountLinkRepository.countByFromAccountIdAndLastSeenAfter(accountId, after);
    }

    public int distanceFromKnownFraudNode(String accountId) {
        List<String> knownFraudAccounts = fraudCaseRepository.findByStatusIn(List.of("OPEN", "RESOLVED"))
                .stream()
                .map(FraudCase::getAccountId)
                .filter(fraudAccount -> fraudAccount != null && !fraudAccount.isBlank())
                .distinct()
                .toList();

        if (knownFraudAccounts.isEmpty()) {
            return MAX_DEPTH + 1;
        }

        if (knownFraudAccounts.contains(accountId)) {
            return 0;
        }

        List<Document> pipeline = Arrays.asList(
                new Document("$match", new Document("fromAccountId", accountId)),
                new Document("$graphLookup", new Document("from", "account_links")
                        .append("startWith", "$toAccountId")
                        .append("connectFromField", "toAccountId")
                        .append("connectToField", "fromAccountId")
                        .append("as", "networkChain")
                        .append("maxDepth", MAX_DEPTH)
                        .append("depthField", "hopCount"))
        );

        int minHop = MAX_DEPTH + 1;

        for (Document result : mongoTemplate.getCollection("account_links").aggregate(pipeline)) {
            List<Document> chain = result.getList("networkChain", Document.class, new ArrayList<>());

            for (Document edge : chain) {
                String from = edge.getString("fromAccountId");
                String to = edge.getString("toAccountId");
                Number hopNumber = (Number) edge.get("hopCount");
                int hopCount = hopNumber != null ? hopNumber.intValue() : MAX_DEPTH;

                if (knownFraudAccounts.contains(from) || knownFraudAccounts.contains(to)) {
                    minHop = Math.min(minHop, hopCount + 1);
                }
            }
        }

        return minHop;
    }
}
