package com.example.foodshop.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatisticsResponse {
    
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Map<String, Long> ordersByStatus;
    private Map<String, BigDecimal> revenueByStatus;
    
    public OrderStatisticsResponse(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalOrders = 0L;
        this.totalRevenue = BigDecimal.ZERO;
        this.averageOrderValue = BigDecimal.ZERO;
    }
    
    public void calculateAverageOrderValue() {
        if (totalOrders > 0 && totalRevenue != null) {
            this.averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
}