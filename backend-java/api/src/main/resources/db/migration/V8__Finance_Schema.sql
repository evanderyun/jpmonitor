-- ============================================================================
-- JPM ERP - MODULE: FINANCE (AP & CASH MANAGEMENT) - ENHANCED REPORTING
-- Dependency: V1 (Core), V3 (Procurement)
-- Function: Debt Monitoring, Cash Flow, Cost Analysis
-- ============================================================================

-- 1. COST CATEGORIES (Untuk Laporan Manajerial)
CREATE TABLE cost_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) UNIQUE NOT NULL, -- 'COST-FUEL', 'COST-PARTS'
    name VARCHAR(100) NOT NULL, -- 'Biaya Bahan Bakar', 'Biaya Sparepart'
    type VARCHAR(20) DEFAULT 'OPEX', -- 'OPEX' (Operasional), 'CAPEX' (Modal)
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. CASH & BANK ACCOUNTS
CREATE TABLE cash_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID REFERENCES projects(id),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) DEFAULT 'BANK',
    currency VARCHAR(3) DEFAULT 'IDR',
    balance DECIMAL(19, 2) DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. ACCOUNTS PAYABLE (Hutang Dagang & Expense Recorder)
CREATE TABLE accounts_payable (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ap_number VARCHAR(100) UNIQUE NOT NULL, -- 'AP-2025-0001'
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    
    -- Reporting Context (Crucial for "Biaya per Project per Kategori")
    project_id UUID NOT NULL REFERENCES projects(id), -- Beban Proyek mana?
    cost_category_id UUID REFERENCES cost_categories(id), -- Biaya apa?
    
    -- Source Traceability
    source_module VARCHAR(50) NOT NULL, -- 'INVENTORY', 'MAINTENANCE', 'FUEL'
    reference_id UUID, -- Link to MaintenanceRecord / InventoryTx
    reference_doc_number VARCHAR(100), -- Vendor Invoice No
    
    transaction_date DATE NOT NULL,
    due_date DATE NOT NULL,
    
    total_amount DECIMAL(19, 2) NOT NULL,
    paid_amount DECIMAL(19, 2) DEFAULT 0,
    outstanding_amount DECIMAL(19, 2) GENERATED ALWAYS AS (total_amount - paid_amount) STORED,
    
    status VARCHAR(20) DEFAULT 'UNPAID', -- 'UNPAID', 'PARTIAL', 'PAID', 'OVERDUE'
    notes TEXT,
    
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. PAYMENTS (Cash Out)
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_number VARCHAR(100) UNIQUE NOT NULL, -- 'PAY-2025-001'
    payment_date DATE NOT NULL,
    
    cash_account_id UUID REFERENCES cash_accounts(id),
    supplier_id UUID REFERENCES suppliers(id),
    
    amount DECIMAL(19, 2) NOT NULL,
    payment_method VARCHAR(50),
    reference_number VARCHAR(100),
    
    notes TEXT,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. PAYMENT ALLOCATION
CREATE TABLE payment_allocations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id UUID NOT NULL REFERENCES payments(id),
    ap_id UUID NOT NULL REFERENCES accounts_payable(id),
    
    allocated_amount DECIMAL(19, 2) NOT NULL,
    
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes for Reporting
CREATE INDEX idx_ap_project ON accounts_payable(project_id);
CREATE INDEX idx_ap_category ON accounts_payable(cost_category_id);
CREATE INDEX idx_ap_date ON accounts_payable(transaction_date);