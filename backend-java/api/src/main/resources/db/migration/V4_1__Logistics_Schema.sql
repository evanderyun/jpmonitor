-- ============================================================================
-- JPM ERP - MODULE: LOGISTICS (SHIPMENTS/DO)
-- Dependency: V4 (Inventory), V1 (Core), V5 (Fleet)
-- ============================================================================

CREATE TABLE goods_shipments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    do_number VARCHAR(100) UNIQUE NOT NULL, -- 'DO-2025-001'
    date DATE NOT NULL,
    
    -- Source is always a warehouse/site
    source_location_id UUID REFERENCES locations(id),
    
    -- Target can be another Site or a Vendor (Return)
    target_type VARCHAR(20) NOT NULL, -- 'LOCATION', 'VENDOR'
    target_location_id UUID REFERENCES locations(id),
    target_supplier_id UUID REFERENCES suppliers(id),
    
    -- Transport Details
    transport_provider VARCHAR(20) DEFAULT 'INTERNAL', -- 'INTERNAL', 'EXTERNAL'
    
    -- If Internal
    driver_employee_id UUID, -- Linked to Employees (will be FK in V2 context but table exists)
    vehicle_equipment_id UUID, -- Linked to Equipment (will be FK in V5 context)
    
    -- If External
    external_driver_name VARCHAR(200),
    external_vehicle_desc VARCHAR(200),
    
    police_number VARCHAR(50),
    
    status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED'
    notes TEXT,
    
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE shipment_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shipment_id UUID NOT NULL REFERENCES goods_shipments(id) ON DELETE CASCADE,
    part_id UUID NOT NULL REFERENCES spare_parts(id),
    
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_code_snapshot VARCHAR(50), -- Optional: If item is for specific unit
    notes TEXT,
    
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_shipments_do ON goods_shipments(do_number);
CREATE INDEX idx_shipments_date ON goods_shipments(date);
