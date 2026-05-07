package com.bank.frauddetection.event;

import com.bank.frauddetection.service.RiskCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionGraphUpdateListener {

    private final RiskCacheService riskCacheService;

    @Async
    @EventListener
    public void onTransactionGraphUpdate(TransactionGraphUpdateEvent event) {
     /*   riskCacheService.invalidate(event.getFromAccountId());
        riskCacheService.invalidate(event.getToAccountId());*/
        log.info("Invalidated cached risk scores after graph update. transactionId={}, from={}, to={}",
                event.getTransactionId(), event.getFromAccountId(), event.getToAccountId());
    }
}
