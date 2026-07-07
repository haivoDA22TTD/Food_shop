package com.example.foodshop.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class OrderStatisticsResponse {
    
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Map<String, Long> ordersByStatus;
    private Map<String, BigDecimal> revenueByStatus;
    
    // Constructors
    public OrderStatisticsResponse() {
    }
    
    public OrderStatisticsResponse(LocalDate startDate, LocalDate endDate, Long totalOrders,
                                  BigDecimal totalRevenue, BigDecimal averageOrderValue,
                                  Map<String, Long> ordersByStatus, Map<String, BigDecimal> revenueByStatus) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.averageOrderValue = averageOrderValue;
        this.ordersByStatus = ordersByStatus;
        this.revenueByStatus = revenueByStatus;
    }
    
    public OrderStatisticsResponse(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalOrders = 0L;
        this.totalRevenue = BigDecimal.ZERO;
        this.averageOrderValue = BigDecimal.ZERO;
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
    
    public Long getTotalOrders() {
        return totalOrders;
    }
    
    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }
    
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }
    
    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }
    
    public Map<String, Long> getOrdersByStatus() {
        return ordersByStatus;
    }
    
    public void setOrdersByStatus(Map<String, Long> ordersByStatus) {
        this.ordersByStatus = ordersByStatus;
    }
    
    public Map<String, BigDecimal> getRevenueByStatus() {
        return revenueByStatus;
    }
    
    public void setRevenueByStatus(Map<String, BigDecimal> revenueByStatus) {
        this.revenueByStatus = revenueByStatus;
    }
    
    public void calculateAverageOrderValue() {
        if (totalOrders > 0 && totalRevenue != null) {
            this.averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
}