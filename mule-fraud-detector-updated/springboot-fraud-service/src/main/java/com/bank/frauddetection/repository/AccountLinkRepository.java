package com.bank.frauddetection.repository;

import com.bank.frauddetection.model.AccountLink;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AccountLinkRepository extends MongoRepository<AccountLink, String> {

    Optional<AccountLink> findByFromAccountIdAndToAccountId(String fromAccountId, String toAccountId);

    List<AccountLink> findByFromAccountId(String fromAccountId);

    List<AccountLink> findByToAccountId(String toAccountId);

    long countByFromAccountId(String fromAccountId);

    long countByToAccountId(String toAccountId);

    long countByFromAccountIdAndLastSeenAfter(String fromAccountId, LocalDateTime after);
}
