package com.bank.frauddetection.repository;

import com.bank.frauddetection.model.FraudCase;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FraudCaseRepository extends MongoRepository<FraudCase, String> {

    Optional<FraudCase> findByReferenceId(String referenceId);

    List<FraudCase> findByStatus(String status);

    List<FraudCase> findByStatusIn(List<String> statuses);
}
