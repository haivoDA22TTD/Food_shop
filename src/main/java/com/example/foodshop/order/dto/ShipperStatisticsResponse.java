package com.example.foodshop.order.dto;

public class ShipperStatisticsResponse {
    
    private Long totalShippers;
    private Long activeShippers;
    private Long availableShippers;
    private Long busyShippers;
    private Double averageRating;
    private Long totalDeliveries;
    private Long successfulDeliveries;
    private Double overallSuccessRate;
    
    // Constructors
    public ShipperStatisticsResponse() {
    }
    
    // Getters and Setters
    public Long getTotalShippers() {
        return totalShippers;
    }
    
    public void setTotalShippers(Long totalShippers) {
        this.totalShippers = totalShippers;
    }
    
    public Long getActiveShippers() {
        return activeShippers;
    }
    
    public void setActiveShippers(Long activeShippers) {
        this.activeShippers = activeShippers;
    }
    
    public Long getAvailableShippers() {
        return availableShippers;
    }
    
    public void setAvailableShippers(Long availableShippers) {
        this.availableShippers = availableShippers;
    }
    
    public Long getBusyShippers() {
        return busyShippers;
    }
    
    public void setBusyShippers(Long busyShippers) {
        this.busyShippers = busyShippers;
    }
    
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    public Long getTotalDeliveries() {
        return totalDeliveries;
    }
    
    public void setTotalDeliveries(Long totalDeliveries) {
        this.totalDeliveries = totalDeliveries;
    }
    
    public Long getSuccessfulDeliveries() {
        return successfulDeliveries;
    }
    
    public void setSuccessfulDeliveries(Long successfulDeliveries) {
        this.successfulDeliveries = successfulDeliveries;
    }
    
    public Double getOverallSuccessRate() {
        return overallSuccessRate;
    }
    
    public void setOverallSuccessRate(Double overallSuccessRate) {
        this.overallSuccessRate = overallSuccessRate;
    }
}
