-- ============================================================================
-- JPM ERP - MODULE: INVENTORY (LOGISTICS) - REVISED
-- Dependency: V1 (Core), V3 (Procurement)
-- ============================================================================

CREATE TABLE spare_parts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    part_number VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    brand VARCHAR(100),
    category VARCHAR(50),
    current_stock INTEGER DEFAULT 0 CHECK (current_stock >= 0),
    min_stock_level INTEGER DEFAULT 0,
    unit VARCHAR(20) NOT NULL,
    
    location_id UUID REFERENCES locations(id), -- Warehouse/Site
    rack_code VARCHAR(50), -- Specific Rack/Bin Location (e.g., A-01)
    
    preferred_supplier_id UUID REFERENCES suppliers(id),
    
    -- Cached Prices
    current_price_cash DECIMAL(19, 2), 
    current_price_credit DECIMAL(19, 2),
    
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE part_price_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    part_id UUID NOT NULL REFERENCES spare_parts(id),
    supplier_id UUID REFERENCES suppliers(id),
    price_cash DECIMAL(19, 2) NOT NULL DEFAULT 0,
    price_credit DECIMAL(19, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'IDR',
    effective_from TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    effective_to TIMESTAMPTZ,
    reason TEXT,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE inventory_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trx_number VARCHAR(100) UNIQUE NOT NULL,
    date DATE NOT NULL,
    type VARCHAR(50) NOT NULL, 
    part_id UUID NOT NULL REFERENCES spare_parts(id),
    quantity INTEGER NOT NULL CHECK (quantity != 0),
    
    applied_price DECIMAL(19, 2), 
    payment_method VARCHAR(20) DEFAULT 'CASH',
    due_date DATE, -- For Credit Payment
    
    reference_id VARCHAR(100), 
    supplier_id UUID REFERENCES suppliers(id), 
    project_id UUID REFERENCES projects(id), 
    
    notes TEXT,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_part_number ON spare_parts(part_number);
CREATE INDEX idx_inv_trx_date ON inventory_transactions(date);
CREATE INDEX idx_price_eff ON part_price_history(part_id, effective_from);