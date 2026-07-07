-- ============================================
-- Payment Sagas Table Migration Script
-- ============================================
-- Purpose: Create payment_sagas table for Saga Pattern implementation
-- Version: 1.0
-- Date: 2024-01-01
-- ============================================

USE food_shop_db;

-- Drop table if exists (for clean migration)
-- WARNING: This will delete all saga data!
-- Comment out this line if you want to preserve existing data
-- DROP TABLE IF EXISTS payment_sagas;

-- Create payment_sagas table
CREATE TABLE IF NOT EXISTS payment_sagas (
    -- Primary key
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Saga identification
    saga_id VARCHAR(50) UNIQUE NOT NULL COMMENT 'Unique saga identifier (SAGA-xxx)',
    
    -- Related entities
    payment_id BIGINT COMMENT 'Associated payment ID',
    order_id BIGINT NOT NULL COMMENT 'Order ID being processed',
    user_id BIGINT NOT NULL COMMENT 'User who initiated the payment',
    voucher_id BIGINT COMMENT 'Voucher ID if used',
    
    -- Saga state
    status VARCHAR(30) NOT NULL COMMENT 'Current saga status (STARTED, COMPLETED, FAILED, etc.)',
    current_step VARCHAR(30) COMMENT 'Current saga step (VALIDATE_ORDER, RESERVE_VOUCHER, etc.)',
    
    -- Error handling
    error_message TEXT COMMENT 'Error message if saga failed',
    compensation_count INT DEFAULT 0 COMMENT 'Number of compensation attempts',
    retry_count INT DEFAULT 0 COMMENT 'Number of retry attempts',
    max_retries INT DEFAULT 3 COMMENT 'Maximum number of retries allowed',
    
    -- Saga data
    saga_data TEXT COMMENT 'JSON data for saga context (payment method, voucher code, etc.)',
    
    -- Timestamps
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Saga creation time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    completed_at DATETIME COMMENT 'Saga completion time',
    
    -- Indexes for performance
    INDEX idx_saga_id (saga_id),
    INDEX idx_payment_id (payment_id),
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_status_created (status, created_at),
    
    -- Foreign key constraints
    CONSTRAINT fk_payment_saga_payment 
        FOREIGN KEY (payment_id) 
        REFERENCES payments(id) 
        ON DELETE SET NULL
        ON UPDATE CASCADE
        
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Payment sagas for distributed transaction management';

-- ============================================
-- Verify table creation
-- ============================================

-- Show table structure
DESCRIBE payment_sagas;

-- Show indexes
SHOW INDEX FROM payment_sagas;

-- Count existing records
SELECT COUNT(*) as total_sagas FROM payment_sagas;

-- ============================================
-- Sample queries for testing
-- ============================================

-- Get all saga statuses
-- SELECT status, COUNT(*) as count 
-- FROM payment_sagas 
-- GROUP BY status;

-- Get active sagas
-- SELECT * FROM payment_sagas 
-- WHERE status IN ('STARTED', 'ORDER_VALIDATED', 'VOUCHER_RESERVED', 'PAYMENT_CREATED', 'PAYMENT_PROCESSED')
-- ORDER BY created_at DESC;

-- Get failed sagas
-- SELECT * FROM payment_sagas 
-- WHERE status = 'FAILED'
-- ORDER BY created_at DESC;

-- Get sagas by user
-- SELECT * FROM payment_sagas 
-- WHERE user_id = ?
-- ORDER BY created_at DESC;

-- Get saga by payment
-- SELECT * FROM payment_sagas 
-- WHERE payment_id = ?;

-- Get saga by order
-- SELECT * FROM payment_sagas 
-- WHERE order_id = ?;

-- ============================================
-- Cleanup queries (use with caution!)
-- ============================================

-- Delete old completed sagas (older than 90 days)
-- DELETE FROM payment_sagas 
-- WHERE status = 'COMPLETED' 
--   AND completed_at < DATE_SUB(NOW(), INTERVAL 90 DAY);

-- Delete old failed sagas (older than 30 days)
-- DELETE FROM payment_sagas 
-- WHERE status IN ('FAILED', 'COMPENSATED') 
--   AND created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- ============================================
-- Rollback script (if needed)
-- ============================================

-- To rollback this migration, run:
-- DROP TABLE IF EXISTS payment_sagas;

-- ============================================
-- Migration complete!
-- ============================================

SELECT 'Payment Sagas table created successfully!' as message;
