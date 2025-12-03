-- Migration V13: Enhance shipment_items table (Created in V4.1)
-- Adds pricing and audit columns missing in initial definition

-- Add unit_price if not exists
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='shipment_items' AND column_name='unit_price') THEN
        ALTER TABLE shipment_items ADD COLUMN unit_price NUMERIC(15, 2);
    END IF;
END $$;

-- Add updated_at if not exists
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='shipment_items' AND column_name='updated_at') THEN
        ALTER TABLE shipment_items ADD COLUMN updated_at TIMESTAMPTZ DEFAULT NOW();
    END IF;
END $$;

-- Ensure indexes exist
CREATE INDEX IF NOT EXISTS idx_shipment_items_shipment ON shipment_items(shipment_id);
CREATE INDEX IF NOT EXISTS idx_shipment_items_part ON shipment_items(part_id);

COMMENT ON TABLE shipment_items IS 'Items included in goods shipments for inventory tracking';
COMMENT ON COLUMN shipment_items.quantity IS 'Quantity of parts shipped (triggers stock reduction)';