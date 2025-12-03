-- ============================================================================
-- JPM ERP - MODULE: HR (HUMAN RESOURCES)
-- Dependency: V1 (Core)
-- ============================================================================

CREATE TABLE employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    position VARCHAR(100),
    department VARCHAR(100),
    email VARCHAR(200),
    phone VARCHAR(50),
    status VARCHAR(20) DEFAULT 'Active',
    
    -- Integrations
    user_id UUID REFERENCES users(id), -- Link to System User (if they have login access)
    current_project_id UUID REFERENCES projects(id), -- Assignment
    location_id UUID REFERENCES locations(id), -- Base Location
    
    joined_date DATE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_employees_code ON employees(employee_code);
CREATE INDEX idx_employees_project ON employees(current_project_id);
