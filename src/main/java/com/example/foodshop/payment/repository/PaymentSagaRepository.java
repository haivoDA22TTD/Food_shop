package com.example.foodshop.payment.repository;

import com.example.foodshop.payment.entity.PaymentSaga;
import com.example.foodshop.payment.saga.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentSagaRepository extends JpaRepository<PaymentSaga, Long> {
    
    Optional<PaymentSaga> findBySagaId(String sagaId);
    
    Optional<PaymentSaga> findByPaymentId(Long paymentId);
    
    Optional<PaymentSaga> findByOrderId(Long orderId);
    
    List<PaymentSaga> findByStatus(SagaStatus status);
    
    List<PaymentSaga> findByStatusIn(List<SagaStatus> statuses);
    
    @Query("SELECT ps FROM PaymentSaga ps WHERE ps.status IN :statuses AND ps.updatedAt < :timeout")
    List<PaymentSaga> findStaleSagas(@Param("statuses") List<SagaStatus> statuses, 
                                     @Param("timeout") LocalDateTime timeout);
    
    @Query("SELECT ps FROM PaymentSaga ps WHERE ps.status = :status AND ps.retryCount < ps.maxRetries")
    List<PaymentSaga> findRetryableSagas(@Param("status") SagaStatus status);
    
    long countByStatus(SagaStatus status);
    
    @Query("SELECT ps.status as status, COUNT(ps) as count FROM PaymentSaga ps GROUP BY ps.status")
    List<Object[]> getSagaStatistics();
}
