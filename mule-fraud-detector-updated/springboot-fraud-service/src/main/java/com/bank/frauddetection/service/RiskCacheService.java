package com.bank.frauddetection.service;

import com.bank.frauddetection.dto.MuleCheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RiskCacheService {

    private final RedisTemplate<String, MuleCheckResponse> redisTemplate;

    @Value("${app.redis.enabled:true}")
    private boolean redisEnabled;

    private static final String PREFIX = "risk:account:";

    public MuleCheckResponse get(String accountId) {
        if (!redisEnabled) return null;

        return redisTemplate.opsForValue().get(PREFIX + accountId);
    }

    public void save(String accountId, MuleCheckResponse response) {
        if (!redisEnabled) return;

        redisTemplate.opsForValue().set(
                PREFIX + accountId,
                response,
                Duration.ofHours(1)
        );
    }
}