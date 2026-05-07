package com.bank.frauddetection.service;

import com.bank.frauddetection.dto.MuleCheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RiskCacheService {

    private final RedisTemplate<String, MuleCheckResponse> redisTemplate;

    private static final String PREFIX = "risk:account:";

    public MuleCheckResponse get(String accountId) {
        return redisTemplate.opsForValue().get(key(accountId));
    }

    public void save(String accountId, MuleCheckResponse response) {
        redisTemplate.opsForValue().set(key(accountId), response, Duration.ofHours(1));
    }

    public void invalidate(String accountId) {
        redisTemplate.delete(key(accountId));
    }

    private String key(String accountId) {
        return PREFIX + accountId;
    }
}
