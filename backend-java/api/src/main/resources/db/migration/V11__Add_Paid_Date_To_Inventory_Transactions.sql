-- Add paid_date column to inventory_transactions
-- This field tracks when a credit purchase was actually paid

ALTER TABLE inventory_transactions 
ADD COLUMN paid_date DATE;

-- Add index for quick lookup of unpaid transactions
CREATE INDEX idx_inv_trx_paid_date ON inventory_transactions(paid_date) WHERE paid_date IS NULL;

COMMENT ON COLUMN inventory_transactions.paid_date IS 'Date when credit payment was completed (NULL = unpaid)';
