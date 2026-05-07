package com.bank.frauddetection.repository;

import com.bank.frauddetection.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends MongoRepository<Transaction, String> {

    long countByToAccountIdAndTimestampAfter(String toAccountId, LocalDateTime after);

    long countByFromAccountIdAndTimestampAfter(String fromAccountId, LocalDateTime after);

    List<Transaction> findByToAccountIdAndTimestampAfter(String toAccountId, LocalDateTime after);

    List<Transaction> findByFromAccountIdAndTimestampAfter(String fromAccountId, LocalDateTime after);

    Optional<Transaction> findTopByToAccountIdOrderByTimestampDesc(String toAccountId);
}
