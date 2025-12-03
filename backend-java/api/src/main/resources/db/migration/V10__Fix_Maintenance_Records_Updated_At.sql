-- Add missing updated_at column to maintenance_records table
-- This column is required by BaseEntity which MaintenanceRecord extends

ALTER TABLE maintenance_records 
ADD COLUMN updated_at TIMESTAMPTZ DEFAULT NOW();

-- Add comment for clarity
COMMENT ON COLUMN maintenance_records.updated_at IS 'Last modification timestamp (required by BaseEntity)';
