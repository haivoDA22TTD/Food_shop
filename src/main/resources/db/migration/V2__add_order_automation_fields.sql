-- Add automation fields to orders table
-- This migration adds support for order automation features

-- Add auto_confirmed field
ALTER TABLE orders 
ADD COLUMN auto_confirmed BOOLEAN DEFAULT FALSE 
COMMENT 'Indicates if order was automatically confirmed';

-- Add cancellation_reason field
ALTER TABLE orders 
ADD COLUMN cancellation_reason TEXT 
COMMENT 'Reason for order cancellation (manual or automatic)';

-- Add index for auto-cancel query optimization
CREATE INDEX idx_orders_status_created 
ON orders(status, created_at) 
COMMENT 'Index for finding expired pending orders';

-- Update existing orders to have auto_confirmed = false
UPDATE orders 
SET auto_confirmed = FALSE 
WHERE auto_confirmed IS NULL;
