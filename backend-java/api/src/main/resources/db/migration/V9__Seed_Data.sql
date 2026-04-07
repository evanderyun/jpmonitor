-- JPM ERP - SEED DATA
-- 1. ROLES
INSERT INTO roles (id, code, name, description, permissions) VALUES 
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ROLE_SUPER_ADMIN', 'Super Administrator', 'Full System Access', '["*"]'),
('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'ROLE_MANAGER', 'Manager', 'Approval & View', '["VIEW_ALL", "APPROVE"]'),
('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'ROLE_STAFF', 'Staff', 'Operational Input', '["INPUT_DATA"]');

-- NOTE: Admin user is created by JpmErpApplication.initData() with a secure random password.
-- Do NOT insert admin users in migrations -- this exposes credentials in git history.

-- 2. DEFAULT COST CATEGORIES
INSERT INTO cost_categories (code, name, type, description) VALUES
('COST-FUEL', 'Bahan Bakar (Fuel)', 'OPEX', 'Pembelian solar industri'),
('COST-PARTS', 'Spareparts', 'OPEX', 'Pembelian suku cadang'),
('COST-SERVICE', 'Jasa Luar (External Service)', 'OPEX', 'Pembelian jasa bengkel/vendor'),
('COST-SALARY', 'Gaji & Upah', 'OPEX', 'Gaji karyawan bulanan'),
('COST-MEAL', 'Uang Makan', 'OPEX', 'Uang makan harian lapangan');
