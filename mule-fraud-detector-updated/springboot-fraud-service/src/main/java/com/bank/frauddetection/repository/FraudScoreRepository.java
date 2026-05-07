package com.bank.frauddetection.repository;

import com.bank.frauddetection.model.FraudScore;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface FraudScoreRepository extends MongoRepository<FraudScore, String> {
    Optional<FraudScore> findTopByAccountIdOrderByComputedAtDesc(String accountId);
}
