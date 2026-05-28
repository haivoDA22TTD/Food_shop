package com.example.foodshop.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shippers", indexes = {
        @Index(name = "idx_phone", columnList = "phone"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
public class Shipper {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "user_id", unique = true)
    private Long userId; // Link to Identity Service user
    
    @Column(nullable = false, unique = true, length = 20)
    private String phone;
    
    @Column(length = 100)
    private String email;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipperStatus status = ShipperStatus.AVAILABLE;
    
    @Column(name = "vehicle_type", length = 50)
    private String vehicleType; // Motorbike, Car, Bicycle
    
    @Column(name = "vehicle_number", length = 20)
    private String vehicleNumber; // License plate
    
    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;
    
    @Column(name = "successful_deliveries")
    private Integer successfulDeliveries = 0;
    
    @Column(name = "rating", precision = 3, scale = 2)
    private Double rating = 5.0; // Default 5.0
    
    @Column(name = "total_ratings")
    private Integer totalRatings = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Shipper() {
    }
    
    public Shipper(String name, String phone, String email, String address) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
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
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public void incrementDeliveries() {
        this.totalDeliveries++;
    }
    
    public void incrementSuccessfulDeliveries() {
        this.successfulDeliveries++;
        this.totalDeliveries++;
    }
    
    public void updateRating(double newRating) {
        if (this.totalRatings == 0) {
            this.rating = newRating;
            this.totalRatings = 1;
        } else {
            double totalScore = this.rating * this.totalRatings;
            this.totalRatings++;
            this.rating = (totalScore + newRating) / this.totalRatings;
        }
    }
    
    public double getSuccessRate() {
        if (totalDeliveries == 0) return 100.0;
        return (successfulDeliveries * 100.0) / totalDeliveries;
    }
    
    public boolean isAvailable() {
        return isActive && status == ShipperStatus.AVAILABLE;
    }
}
