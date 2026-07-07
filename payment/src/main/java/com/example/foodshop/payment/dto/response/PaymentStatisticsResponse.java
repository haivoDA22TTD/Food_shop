package com.example.foodshop.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class PaymentStatisticsResponse {
    
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalPayments;
    private BigDecimal totalRevenue;
    private BigDecimal averagePaymentAmount;
    private Map<String, Long> paymentsByStatus;
    private Map<String, BigDecimal> revenueByStatus;
    private Map<String, Long> paymentsByMethod;
    
    // Constructors
    public PaymentStatisticsResponse() {
        this.paymentsByStatus = new HashMap<>();
        this.revenueByStatus = new HashMap<>();
        this.paymentsByMethod = new HashMap<>();
    }
    
    public PaymentStatisticsResponse(LocalDate startDate, LocalDate endDate) {
        this();
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Getters and Setters
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public Long getTotalPayments() {
        return totalPayments;
    }
    
    public void setTotalPayments(Long totalPayments) {
        this.totalPayments = totalPayments;
    }
    
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public BigDecimal getAveragePaymentAmount() {
        return averagePaymentAmount;
    }
    
    public void setAveragePaymentAmount(BigDecimal averagePaymentAmount) {
        this.averagePaymentAmount = averagePaymentAmount;
    }
    
    public Map<String, Long> getPaymentsByStatus() {
        return paymentsByStatus;
    }
    
    public void setPaymentsByStatus(Map<String, Long> paymentsByStatus) {
        this.paymentsByStatus = paymentsByStatus;
    }
    
    public Map<String, BigDecimal> getRevenueByStatus() {
        return revenueByStatus;
    }
    
    public void setRevenueByStatus(Map<String, BigDecimal> revenueByStatus) {
        this.revenueByStatus = revenueByStatus;
    }
    
    public Map<String, Long> getPaymentsByMethod() {
        return paymentsByMethod;
    }
    
    public void setPaymentsByMethod(Map<String, Long> paymentsByMethod) {
        this.paymentsByMethod = paymentsByMethod;
    }
    
    public void calculateAveragePaymentAmount() {
        if (totalPayments != null && totalPayments > 0 && totalRevenue != null) {
            this.averagePaymentAmount = totalRevenue.divide(
                BigDecimal.valueOf(totalPayments), 2, java.math.RoundingMode.HALF_UP);
        } else {
            this.averagePaymentAmount = BigDecimal.ZERO;
        }
    }
}
