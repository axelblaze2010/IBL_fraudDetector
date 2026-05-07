package com.bank.frauddetection.repository;

import com.bank.frauddetection.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface AccountRepository extends MongoRepository<Account, String> {
    Optional<Account> findByAccountId(String accountId);
}
