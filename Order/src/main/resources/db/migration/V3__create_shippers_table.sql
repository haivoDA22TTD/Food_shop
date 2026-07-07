-- Create shippers table
CREATE TABLE IF NOT EXISTS shippers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100),
    address TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    vehicle_type VARCHAR(50),
    vehicle_number VARCHAR(20),
    total_deliveries INT DEFAULT 0,
    successful_deliveries INT DEFAULT 0,
    rating DECIMAL(3,2) DEFAULT 5.0,
    total_ratings INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_phone (phone),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add shipper-related columns to orders table
ALTER TABLE orders 
ADD COLUMN shipper_id BIGINT AFTER auto_confirmed,
ADD COLUMN assigned_at TIMESTAMP NULL AFTER shipper_id,
ADD COLUMN picked_up_at TIMESTAMP NULL AFTER assigned_at,
ADD COLUMN delivered_at TIMESTAMP NULL AFTER picked_up_at,
ADD COLUMN delivery_notes TEXT AFTER delivered_at,
ADD INDEX idx_shipper_id (shipper_id);

-- Add foreign key constraint (optional, for referential integrity)
-- ALTER TABLE orders 
-- ADD CONSTRAINT fk_orders_shipper 
-- FOREIGN KEY (shipper_id) REFERENCES shippers(id) 
-- ON DELETE SET NULL;
