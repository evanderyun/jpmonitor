-- ============================================================================
-- JPM ERP - MODULE: MINING PRODUCTION (COAL CHAIN)
-- Dependency: V1 (Core), V5 (Fleet)
-- Scope: Pit to Port (Digging, Hauling, Stockpiling, Quality)
-- ============================================================================

-- 1. MASTER DATA: MINING LOCATIONS
CREATE TABLE pits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID REFERENCES projects(id),
    code VARCHAR(50) UNIQUE NOT NULL, -- 'PIT-A1'
    name VARCHAR(200) NOT NULL,
    block VARCHAR(50),
    strip_ratio_plan DECIMAL(10, 2), -- Target SR
    status VARCHAR(50) DEFAULT 'Active',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE stockpiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID REFERENCES projects(id),
    code VARCHAR(50) UNIQUE NOT NULL, -- 'ROM-01'
    name VARCHAR(200) NOT NULL,
    location_id UUID REFERENCES locations(id), -- Physical Location
    
    -- Capacity Mgmt
    capacity_mt DECIMAL(19, 2) DEFAULT 0,
    current_volume_mt DECIMAL(19, 2) DEFAULT 0, -- Updated by triggers/service
    
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. SHIFT REPORTING (HIGH LEVEL SUMMARY)
-- Matches current Frontend 'ProductionView'
CREATE TABLE production_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    date DATE NOT NULL,
    shift VARCHAR(20) NOT NULL, -- 'Day', 'Night'
    pit_id UUID NOT NULL REFERENCES pits(id),
    supervisor_id UUID REFERENCES employees(id),
    
    -- Output
    overburden_bcm DECIMAL(19, 2) DEFAULT 0,
    coal_mt DECIMAL(19, 2) DEFAULT 0,
    
    -- Calculated
    stripping_ratio DECIMAL(10, 2) GENERATED ALWAYS AS (
        CASE WHEN coal_mt > 0 THEN overburden_bcm / coal_mt ELSE 0 END
    ) STORED,
    
    status VARCHAR(50) DEFAULT 'Draft', -- 'Draft', 'Approved'
    notes TEXT,
    
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. HAULING LOGS (DETAILED RITASE)
-- Future-proof for Mobile App / IoT
CREATE TABLE hauling_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trx_number VARCHAR(100) UNIQUE NOT NULL, -- Ticket Number
    date TIMESTAMPTZ NOT NULL,
    
    -- Unit & Operator
    equipment_id UUID NOT NULL REFERENCES equipment(id), -- Dump Truck
    operator_id UUID REFERENCES employees(id), -- Driver
    
    -- Route
    origin_pit_id UUID REFERENCES pits(id),
    destination_stockpile_id UUID REFERENCES stockpiles(id),
    distance_km DECIMAL(10, 2),
    
    -- Payload
    gross_weight DECIMAL(10, 2),
    tare_weight DECIMAL(10, 2),
    net_weight DECIMAL(10, 2) GENERATED ALWAYS AS (gross_weight - tare_weight) STORED,
    
    -- Verification
    loader_equipment_id UUID REFERENCES equipment(id), -- Excavator/Loader
    status VARCHAR(20) DEFAULT 'Verified',
    
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. QUALITY CONTROL (COAL QUALITY)
CREATE TABLE coal_quality_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sample_date DATE NOT NULL,
    reference_code VARCHAR(100), -- Lab Report No
    
    -- Source
    source_type VARCHAR(20) NOT NULL, -- 'PIT', 'STOCKPILE', 'BARGE'
    pit_id UUID REFERENCES pits(id),
    stockpile_id UUID REFERENCES stockpiles(id),
    
    -- Parameters (Standard Coal Analysis)
    tm_ar DECIMAL(5, 2), -- Total Moisture (As Received)
    im_adb DECIMAL(5, 2), -- Inherent Moisture (Air Dried)
    ash_adb DECIMAL(5, 2), -- Ash Content
    vm_adb DECIMAL(5, 2), -- Volatile Matter
    fc_adb DECIMAL(5, 2), -- Fixed Carbon
    ts_adb DECIMAL(5, 2), -- Total Sulphur
    gcv_adb DECIMAL(10, 2), -- Gross Calorific Value
    gcv_ar DECIMAL(10, 2), -- GCV As Received
    
    surveyor_name VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_production_date ON production_records(date);
CREATE INDEX idx_hauling_date ON hauling_logs(date);
CREATE INDEX idx_hauling_eq ON hauling_logs(equipment_id);
