-- ============================================================================
-- JPM ERP - SEED DATA
-- Purpose: Initialize default Roles and Super Admin for first-time setup
-- ============================================================================

-- 1. ROLES
INSERT INTO roles (id, code, name, description, permissions) VALUES 
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ROLE_SUPER_ADMIN', 'Super Administrator', 'Full System Access', '["*"]'),
('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'ROLE_MANAGER', 'Manager', 'Approval & View', '["VIEW_ALL", "APPROVE"]'),
('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'ROLE_STAFF', 'Staff', 'Operational Input', '["INPUT_DATA"]');

-- 2. SUPER ADMIN USER (Password: admin123)
INSERT INTO users (id, username, email, password_hash, full_name, role_id, is_active) VALUES 
(uuid_generate_v4(), 'admin', 'admin@jpm.local', '$2a$10$4ZXpXSD2M6Z.r9T8A021oOYSpsR69MYxbgfBgmZTTO0uM2v9RgWlW', 'System Administrator', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', true);

-- 3. DEFAULT COST CATEGORIES (For Finance)
INSERT INTO cost_categories (code, name, type, description) VALUES
('COST-FUEL', 'Bahan Bakar (Fuel)', 'OPEX', 'Pembelian solar industri'),
('COST-PARTS', 'Spareparts', 'OPEX', 'Pembelian suku cadang'),
('COST-SERVICE', 'Jasa Luar (External Service)', 'OPEX', 'Jasa bengkel/vendor'),
('COST-SALARY', 'Gaji & Upah', 'OPEX', 'Gaji karyawan bulanan'),
('COST-MEAL', 'Uang Makan', 'OPEX', 'Uang makan harian lapangan');
