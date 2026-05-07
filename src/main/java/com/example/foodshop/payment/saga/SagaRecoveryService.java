package com.example.foodshop.payment.saga;

import com.example.foodshop.payment.entity.PaymentSaga;
import com.example.foodshop.payment.repository.PaymentSagaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service to recover stale or failed sagas
 */
@Service
public class SagaRecoveryService {
    
    private static final Logger log = LoggerFactory.getLogger(SagaRecoveryService.class);
    
    @Autowired
    private PaymentSagaRepository sagaRepository;
    
    @Autowired
    private PaymentSagaOrchestrator sagaOrchestrator;
    
    @Value("${app.saga.timeout-minutes:30}")
    private int sagaTimeoutMinutes;
    
    @Value("${app.saga.recovery-enabled:true}")
    private boolean recoveryEnabled;
    
    /**
     * Recover stale sagas every 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    @Transactional
    public void recoverStaleSagas() {
        if (!recoveryEnabled) {
            return;
        }
        
        try {
            log.info("Starting saga recovery process");
            
            LocalDateTime timeout = LocalDateTime.now().minusMinutes(sagaTimeoutMinutes);
            
            List<SagaStatus> activeStatuses = List.of(
                SagaStatus.STARTED,
                SagaStatus.ORDER_VALIDATED,
                SagaStatus.VOUCHER_RESERVED,
                SagaStatus.PAYMENT_CREATED,
                SagaStatus.PAYMENT_PROCESSED
            );
            
            List<PaymentSaga> staleSagas = sagaRepository.findStaleSagas(activeStatuses, timeout);
            
            if (staleSagas.isEmpty()) {
                log.info("No stale sagas found");
                return;
            }
            
            log.warn("Found {} stale sagas", staleSagas.size());
            
            for (PaymentSaga saga : staleSagas) {
                recoverSaga(saga);
            }
            
            log.info("Saga recovery process completed");
            
        } catch (Exception e) {
            log.error("Error in saga recovery process: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Recover a single saga
     */
    @Transactional
    public void recoverSaga(PaymentSaga saga) {
        try {
            log.info("Recovering saga: {} (status: {}, step: {})", 
                    saga.getSagaId(), saga.getStatus(), saga.getCurrentStep());
            
            if (saga.canRetry()) {
                // Try to retry the saga
                saga.incrementRetry();
                sagaRepository.save(saga);
                
                log.info("Retrying saga: {} (attempt {}/{})", 
                        saga.getSagaId(), saga.getRetryCount(), saga.getMaxRetries());
                
                try {
                    sagaOrchestrator.executeSaga(saga);
                    log.info("Saga {} recovered successfully", saga.getSagaId());
                } catch (Exception e) {
                    log.error("Saga {} retry failed: {}", saga.getSagaId(), e.getMessage());
                    
                    if (!saga.canRetry()) {
                        // Max retries reached, compensate
                        sagaOrchestrator.compensateSaga(saga, "Max retries reached: " + e.getMessage());
                    }
                }
            } else {
                // Max retries reached, compensate
                log.warn("Saga {} exceeded max retries, compensating", saga.getSagaId());
                sagaOrchestrator.compensateSaga(saga, "Saga timeout - exceeded max retries");
            }
            
        } catch (Exception e) {
            log.error("Error recovering saga {}: {}", saga.getSagaId(), e.getMessage(), e);
        }
    }
    
    /**
     * Retry failed sagas
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void retryFailedSagas() {
        if (!recoveryEnabled) {
            return;
        }
        
        try {
            List<PaymentSaga> retryableSagas = sagaRepository.findRetryableSagas(SagaStatus.FAILED);
            
            if (retryableSagas.isEmpty()) {
                return;
            }
            
            log.info("Found {} retryable failed sagas", retryableSagas.size());
            
            for (PaymentSaga saga : retryableSagas) {
                recoverSaga(saga);
            }
            
        } catch (Exception e) {
            log.error("Error retrying failed sagas: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Clean up old completed sagas
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    @Transactional
    public void cleanupOldSagas() {
        try {
            log.info("Starting saga cleanup process");
            
            LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
            
            List<SagaStatus> finalStatuses = List.of(
                SagaStatus.COMPLETED,
                SagaStatus.COMPENSATED
            );
            
            List<PaymentSaga> oldSagas = sagaRepository.findStaleSagas(finalStatuses, cutoff);
            
            if (!oldSagas.isEmpty()) {
                sagaRepository.deleteAll(oldSagas);
                log.info("Cleaned up {} old sagas", oldSagas.size());
            }
            
        } catch (Exception e) {
            log.error("Error cleaning up old sagas: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Get saga statistics
     */
    public SagaStatistics getSagaStatistics() {
        List<Object[]> stats = sagaRepository.getSagaStatistics();
        
        SagaStatistics statistics = new SagaStatistics();
        
        for (Object[] stat : stats) {
            SagaStatus status = (SagaStatus) stat[0];
            Long count = (Long) stat[1];
            
            statistics.addCount(status, count);
        }
        
        return statistics;
    }
    
    /**
     * Saga statistics DTO
     */
    public static class SagaStatistics {
        private long totalSagas;
        private long activeSagas;
        private long completedSagas;
        private long failedSagas;
        private long compensatedSagas;
        private java.util.Map<String, Long> statusCounts = new java.util.HashMap<>();
        
        public void addCount(SagaStatus status, Long count) {
            statusCounts.put(status.name(), count);
            totalSagas += count;
            
            if (status == SagaStatus.COMPLETED) {
                completedSagas = count;
            } else if (status == SagaStatus.FAILED) {
                failedSagas = count;
            } else if (status == SagaStatus.COMPENSATED) {
                compensatedSagas = count;
            } else if (!status.isFinal()) {
                activeSagas += count;
            }
        }
        
        // Getters
        public long getTotalSagas() { return totalSagas; }
        public long getActiveSagas() { return activeSagas; }
        public long getCompletedSagas() { return completedSagas; }
        public long getFailedSagas() { return failedSagas; }
        public long getCompensatedSagas() { return compensatedSagas; }
        public java.util.Map<String, Long> getStatusCounts() { return statusCounts; }
    }
}
