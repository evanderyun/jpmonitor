-- ============================================================================
-- JPM ERP - MODULE: HSE (HEALTH, SAFETY, ENVIRONMENT)
-- Dependency: V1 (Core), V2 (HR)
-- ============================================================================

CREATE TABLE hse_incidents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    date DATE NOT NULL,
    time TIME,
    
    -- Classification
    type VARCHAR(50) NOT NULL, -- 'NEAR_MISS', 'INJURY', 'PROPERTY_DAMAGE', 'ENVIRONMENTAL'
    severity VARCHAR(20) DEFAULT 'LOW', -- 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    
    -- Location Context
    location_id UUID REFERENCES locations(id),
    project_id UUID REFERENCES projects(id), -- Critical for Project Safety KPI
    location_detail VARCHAR(200), -- "Ramp 4, KM 12"
    
    -- Description
    description TEXT NOT NULL,
    immediate_action TEXT,
    
    -- Investigation Status
    status VARCHAR(50) DEFAULT 'OPEN', -- 'OPEN', 'INVESTIGATING', 'CLOSED'
    reported_by UUID REFERENCES employees(id),
    
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE hse_investigations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_id UUID NOT NULL REFERENCES hse_incidents(id),
    
    root_cause TEXT,
    corrective_action TEXT,
    preventive_action TEXT,
    
    investigator_id UUID REFERENCES employees(id),
    closed_at TIMESTAMPTZ,
    
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_hse_date ON hse_incidents(date);
CREATE INDEX idx_hse_project ON hse_incidents(project_id);
