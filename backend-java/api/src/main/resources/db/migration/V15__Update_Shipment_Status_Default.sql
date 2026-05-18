-- ============================================================================
-- JPM ERP - MIGRATION V15: Update Shipment Status Default
-- Changes the goods_shipments.status default from 'SHIPPED' to 'PENDING'
-- to match the TypeScript union type and actual domain semantics.
-- ============================================================================

-- Update existing rows that still have the old default value 'SHIPPED'
-- to the appropriate initial status 'PENDING'
UPDATE goods_shipments
SET status = 'PENDING'
WHERE status = 'SHIPPED';

-- Change the column default from 'SHIPPED' to 'PENDING'
ALTER TABLE goods_shipments
ALTER COLUMN status SET DEFAULT 'PENDING';
