-- ============================================================================
-- JPM ERP - MODULE: FLEET & MAINTENANCE (FINAL COST STRUCTURE)
-- Dependency: V1, V2, V3, V4
-- Features: Detailed Labor Cost Separation (Mechanic vs Driver)
-- ============================================================================

CREATE TABLE equipment (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    model VARCHAR(100),
    type VARCHAR(100),
    brand VARCHAR(100),
    serial_number VARCHAR(100),
    engine_number VARCHAR(100),
    chassis_number VARCHAR(100),
    plate_number VARCHAR(50),
    manufacture_year INTEGER,
    status VARCHAR(50) DEFAULT 'Operational',
    
    location_id UUID REFERENCES locations(id),
    project_id UUID REFERENCES projects(id),
    
    hour_meter DECIMAL(10, 1) DEFAULT 0 CHECK (hour_meter >= 0),
    kilometer DECIMAL(10, 1) DEFAULT 0,
    owner VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Add late binding FK to Inventory
ALTER TABLE inventory_transactions 
ADD COLUMN equipment_id UUID REFERENCES equipment(id);

CREATE INDEX idx_inv_trx_equipment ON inventory_transactions(equipment_id);

-- Mutations
CREATE TABLE unit_mutations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mutation_number VARCHAR(100) UNIQUE NOT NULL,
    type VARCHAR(50) NOT NULL,
    equipment_id UUID NOT NULL REFERENCES equipment(id),
    equipment_code VARCHAR(50),
    source_location_id UUID REFERENCES locations(id),
    target_location_id UUID REFERENCES locations(id),
    departure_date DATE NOT NULL,
    arrival_date DATE,
    mutation_hm DECIMAL(10, 1),
    notes TEXT,
    driver_name VARCHAR(200),
    transport_unit VARCHAR(100),
    transport_pol_number VARCHAR(50),
    sender_company VARCHAR(200),
    sender_name VARCHAR(200),
    recipient_company VARCHAR(200),
    recipient_name VARCHAR(200),
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Fuel Logs
CREATE TABLE fuel_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    date DATE NOT NULL,
    equipment_id UUID NOT NULL REFERENCES equipment(id),
    project_id UUID REFERENCES projects(id),
    
    liters DECIMAL(10, 2) NOT NULL CHECK (liters > 0),
    hour_meter DECIMAL(10, 1), 
    price_per_liter DECIMAL(19, 2),
    total_cost DECIMAL(19, 2) GENERATED ALWAYS AS (liters * price_per_liter) STORED,
    
    supplier_id UUID REFERENCES suppliers(id),
    reference_doc VARCHAR(100),
    filled_by VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Daily Logs (Timesheet)
CREATE TABLE daily_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    log_date DATE NOT NULL,
    equipment_id UUID NOT NULL REFERENCES equipment(id),
    operator_id UUID REFERENCES employees(id),
    operator_name VARCHAR(200),
    location_id UUID REFERENCES locations(id),
    project_id UUID REFERENCES projects(id),
    
    start_hm DECIMAL(10, 1) NOT NULL CHECK (start_hm >= 0),
    end_hm DECIMAL(10, 1) NOT NULL CHECK (end_hm >= start_hm),
    total_hours DECIMAL(10, 1) GENERATED ALWAYS AS (end_hm - start_hm) STORED,
    
    activity_code VARCHAR(50),
    notes TEXT,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Work Orders
CREATE TABLE work_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    wo_number VARCHAR(100) UNIQUE NOT NULL,
    equipment_id UUID NOT NULL REFERENCES equipment(id),
    project_id UUID REFERENCES projects(id),
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) DEFAULT 'Medium',
    status VARCHAR(50) DEFAULT 'Open',
    description TEXT,
    assigned_mechanic_id UUID REFERENCES employees(id),
    scheduled_date DATE,
    completed_date DATE,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Maintenance Records (Detailed Costing)
CREATE TABLE maintenance_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    work_order_id UUID REFERENCES work_orders(id),
    equipment_id UUID NOT NULL REFERENCES equipment(id),
    service_date DATE NOT NULL,
    description TEXT,
    
    -- 1. MATERIAL COST
    parts_cost DECIMAL(19, 2) DEFAULT 0,
    
    -- 2. LABOR COSTS (INTERNAL)
    mechanic_labor_cost DECIMAL(19, 2) DEFAULT 0, -- Upah/Lembur Mekanik
    driver_labor_cost DECIMAL(19, 2) DEFAULT 0,   -- Upah/Lembur Driver (Storing/Delivery)
    
    -- 3. OPERATIONAL ALLOWANCES
    meal_allowance_cost DECIMAL(19, 2) DEFAULT 0,      -- Uang Makan Tim
    transport_fuel_cost DECIMAL(19, 2) DEFAULT 0,      -- BBM Sarana/Storing
    
    -- 4. EXTERNAL VENDOR
    external_service_cost DECIMAL(19, 2) DEFAULT 0, 
    external_invoice_number VARCHAR(100),
    
    -- TOTAL (Auto Calculated for Integrity)
    total_cost DECIMAL(19, 2) GENERATED ALWAYS AS (
        parts_cost + 
        mechanic_labor_cost + driver_labor_cost + 
        meal_allowance_cost + transport_fuel_cost + 
        external_service_cost
    ) STORED,
    
    hm_at_service DECIMAL(10, 1),
    technician_notes TEXT,
    
    -- HM Reset Logic
    hm_reset_occurred BOOLEAN DEFAULT FALSE,
    final_hm_reading DECIMAL(10, 1),
    
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE maintenance_external_services (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    maintenance_record_id UUID NOT NULL REFERENCES maintenance_records(id),
    external_service_id UUID NOT NULL REFERENCES external_services(id),
    quantity DECIMAL(10, 2) DEFAULT 1,
    applied_price DECIMAL(19, 2) NOT NULL,
    total_line_cost DECIMAL(19, 2) GENERATED ALWAYS AS (quantity * applied_price) STORED,
    notes TEXT
);

CREATE INDEX idx_equipment_code ON equipment(code);
CREATE INDEX idx_daily_logs_eq_date ON daily_logs(equipment_id, log_date);
