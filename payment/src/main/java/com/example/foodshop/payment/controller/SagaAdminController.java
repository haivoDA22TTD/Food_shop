package com.example.foodshop.payment.controller;

import com.example.foodshop.payment.entity.PaymentSaga;
import com.example.foodshop.payment.repository.PaymentSagaRepository;
import com.example.foodshop.payment.saga.PaymentSagaOrchestrator;
import com.example.foodshop.payment.saga.SagaRecoveryService;
import com.example.foodshop.payment.saga.SagaStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sagas")
@PreAuthorize("hasRole('ADMIN')")
public class SagaAdminController {
    
    @Autowired
    private PaymentSagaRepository sagaRepository;
    
    @Autowired
    private PaymentSagaOrchestrator sagaOrchestrator;
    
    @Autowired
    private SagaRecoveryService sagaRecoveryService;
    
    /**
     * Get all sagas with pagination
     */
    @GetMapping
    public ResponseEntity<Page<PaymentSaga>> getAllSagas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) SagaStatus status) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<PaymentSaga> sagas;
        if (status != null) {
            sagas = sagaRepository.findAll(pageable).map(saga -> {
                if (saga.getStatus() == status) {
                    return saga;
                }
                return null;
            });
        } else {
            sagas = sagaRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(sagas);
    }
    
    /**
     * Get saga by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentSaga> getSagaById(@PathVariable Long id) {
        PaymentSaga saga = sagaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Saga not found"));
        
        return ResponseEntity.ok(saga);
    }
    
    /**
     * Get saga by saga ID
     */
    @GetMapping("/saga/{sagaId}")
    public ResponseEntity<PaymentSaga> getSagaBySagaId(@PathVariable String sagaId) {
        PaymentSaga saga = sagaRepository.findBySagaId(sagaId)
            .orElseThrow(() -> new RuntimeException("Saga not found"));
        
        return ResponseEntity.ok(saga);
    }
    
    /**
     * Get saga by payment ID
     */
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<PaymentSaga> getSagaByPaymentId(@PathVariable Long paymentId) {
        PaymentSaga saga = sagaRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new RuntimeException("Saga not found for payment"));
        
        return ResponseEntity.ok(saga);
    }
    
    /**
     * Get saga by order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentSaga> getSagaByOrderId(@PathVariable Long orderId) {
        PaymentSaga saga = sagaRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Saga not found for order"));
        
        return ResponseEntity.ok(saga);
    }
    
    /**
     * Get saga statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<SagaRecoveryService.SagaStatistics> getSagaStatistics() {
        SagaRecoveryService.SagaStatistics statistics = sagaRecoveryService.getSagaStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Retry a failed saga
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<Map<String, String>> retrySaga(@PathVariable Long id) {
        PaymentSaga saga = sagaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Saga not found"));
        
        if (saga.getStatus().isFinal() && saga.getStatus() != SagaStatus.FAILED) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Cannot retry saga in status: " + saga.getStatus()));
        }
        
        sagaRecoveryService.recoverSaga(saga);
        
        return ResponseEntity.ok(Map.of("message", "Saga retry initiated"));
    }
    
    /**
     * Compensate a saga manually
     */
    @PostMapping("/{id}/compensate")
    public ResponseEntity<Map<String, String>> compensateSaga(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        
        PaymentSaga saga = sagaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Saga not found"));
        
        if (saga.getStatus().isFinal()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Cannot compensate saga in final status: " + saga.getStatus()));
        }
        
        String reason = request != null ? request.get("reason") : "Manual compensation by admin";
        sagaOrchestrator.compensateSaga(saga, reason);
        
        return ResponseEntity.ok(Map.of("message", "Saga compensation initiated"));
    }
    
    /**
     * Get active sagas
     */
    @GetMapping("/active")
    public ResponseEntity<List<PaymentSaga>> getActiveSagas() {
        List<SagaStatus> activeStatuses = List.of(
            SagaStatus.STARTED,
            SagaStatus.ORDER_VALIDATED,
            SagaStatus.VOUCHER_RESERVED,
            SagaStatus.PAYMENT_CREATED,
            SagaStatus.PAYMENT_PROCESSED,
            SagaStatus.COMPENSATING
        );
        
        List<PaymentSaga> sagas = sagaRepository.findByStatusIn(activeStatuses);
        
        return ResponseEntity.ok(sagas);
    }
    
    /**
     * Get failed sagas
     */
    @GetMapping("/failed")
    public ResponseEntity<List<PaymentSaga>> getFailedSagas() {
        List<PaymentSaga> sagas = sagaRepository.findByStatus(SagaStatus.FAILED);
        return ResponseEntity.ok(sagas);
    }
    
    /**
     * Trigger saga recovery manually
     */
    @PostMapping("/recovery/trigger")
    public ResponseEntity<Map<String, String>> triggerRecovery() {
        sagaRecoveryService.recoverStaleSagas();
        return ResponseEntity.ok(Map.of("message", "Saga recovery triggered"));
    }
}
