package com.example.foodshop.order.dto;

import com.example.foodshop.order.entity.Shipper;
import com.example.foodshop.order.entity.ShipperStatus;

import java.time.LocalDateTime;

public class ShipperResponse {
    
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String email;
    private String address;
    private ShipperStatus status;
    private String vehicleType;
    private String vehicleNumber;
    private Integer totalDeliveries;
    private Integer successfulDeliveries;
    private Double rating;
    private Integer totalRatings;
    private Boolean isActive;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double successRate;
    
    // Constructors
    public ShipperResponse() {
    }
    
    public ShipperResponse(Shipper shipper) {
        this.id = shipper.getId();
        this.userId = shipper.getUserId();
        this.name = shipper.getName();
        this.phone = shipper.getPhone();
        this.email = shipper.getEmail();
        this.address = shipper.getAddress();
        this.status = shipper.getStatus();
        this.vehicleType = shipper.getVehicleType();
        this.vehicleNumber = shipper.getVehicleNumber();
        this.totalDeliveries = shipper.getTotalDeliveries();
        this.successfulDeliveries = shipper.getSuccessfulDeliveries();
        this.rating = shipper.getRating();
        this.totalRatings = shipper.getTotalRatings();
        this.isActive = shipper.getIsActive();
        this.notes = shipper.getNotes();
        this.createdAt = shipper.getCreatedAt();
        this.updatedAt = shipper.getUpdatedAt();
        this.successRate = shipper.getSuccessRate();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public ShipperStatus getStatus() {
        return status;
    }
    
    public void setStatus(ShipperStatus status) {
        this.status = status;
    }
    
    public String getVehicleType() {
        return vehicleType;
    }
    
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
    
    public String getVehicleNumber() {
        return vehicleNumber;
    }
    
    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
    
    public Integer getTotalDeliveries() {
        return totalDeliveries;
    }
    
    public void setTotalDeliveries(Integer totalDeliveries) {
        this.totalDeliveries = totalDeliveries;
    }
    
    public Integer getSuccessfulDeliveries() {
        return successfulDeliveries;
    }
    
    public void setSuccessfulDeliveries(Integer successfulDeliveries) {
        this.successfulDeliveries = successfulDeliveries;
    }
    
    public Double getRating() {
        return rating;
    }
    
    public void setRating(Double rating) {
        this.rating = rating;
    }
    
    public Integer getTotalRatings() {
        return totalRatings;
    }
    
    public void setTotalRatings(Integer totalRatings) {
        this.totalRatings = totalRatings;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Double getSuccessRate() {
        return successRate;
    }
    
    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }
}
