package com.example.foodshop.order.repository;

import com.example.foodshop.order.entity.Shipper;
import com.example.foodshop.order.entity.ShipperStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper, Long> {
    
    // Find shipper by phone
    Optional<Shipper> findByPhone(String phone);
    
    // Find active shippers
    List<Shipper> findByIsActiveTrue();
    
    // Find shippers by status
    List<Shipper> findByStatus(ShipperStatus status);
    
    // Find available shippers (active and status = AVAILABLE)
    @Query("SELECT s FROM Shipper s WHERE s.isActive = true AND s.status = 'AVAILABLE' ORDER BY s.totalDeliveries ASC")
    List<Shipper> findAvailableShippers();
    
    // Find shippers by status with pagination
    Page<Shipper> findByStatusOrderByCreatedAtDesc(ShipperStatus status, Pageable pageable);
    
    // Find shippers by name containing
    Page<Shipper> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name, Pageable pageable);
    
    // Find shippers by phone containing
    Page<Shipper> findByPhoneContainingOrderByCreatedAtDesc(String phone, Pageable pageable);
    
    // Count active shippers
    long countByIsActiveTrue();
    
    // Count shippers by status
    long countByStatus(ShipperStatus status);
    
    // Find top rated shippers
    @Query("SELECT s FROM Shipper s WHERE s.isActive = true ORDER BY s.rating DESC, s.totalDeliveries DESC")
    List<Shipper> findTopRatedShippers(Pageable pageable);
    
    // Find shippers with high success rate
    @Query("SELECT s FROM Shipper s WHERE s.isActive = true AND s.totalDeliveries > 0 " +
           "ORDER BY (s.successfulDeliveries * 1.0 / s.totalDeliveries) DESC")
    List<Shipper> findShippersWithHighSuccessRate(Pageable pageable);
    
    // Get shipper statistics
    @Query("SELECT COUNT(s), AVG(s.rating), SUM(s.totalDeliveries), SUM(s.successfulDeliveries) " +
           "FROM Shipper s WHERE s.isActive = true")
    Object[] getShipperStatistics();
}

    
    // Find shipper by user ID
    Optional<Shipper> findByUserId(Long userId);
}
