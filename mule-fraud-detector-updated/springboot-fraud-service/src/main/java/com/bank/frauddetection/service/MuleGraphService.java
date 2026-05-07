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
                Integer hopCount = edge.getInteger("hopCount", MAX_DEPTH);

                if (knownFraudAccounts.contains(from) || knownFraudAccounts.contains(to)) {
                    minHop = Math.min(minHop, hopCount + 1);
                }
            }
        }

        return minHop;
    }
}
