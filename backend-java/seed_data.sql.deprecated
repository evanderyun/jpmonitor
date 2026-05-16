-- Clear existing data
DELETE FROM production_records;
DELETE FROM incidents;
DELETE FROM audit_logs;
DELETE FROM maintenance_records;
DELETE FROM inventory_transactions;
DELETE FROM equipment;
DELETE FROM employees;
DELETE FROM suppliers;
DELETE FROM pits;
DELETE FROM stockpiles;
DELETE FROM users;

-- 0. Users (Auth)
-- Password is 'password' (BCrypt encoded)
INSERT INTO users (id, username, password, role, created_at, updated_at) VALUES
(uuid_generate_v4(), 'admin', '$2a$10$Bcpnhg3daz.lVEKrxFLZZuRlN.pS.Vthp7oZ6gmKBzC2Dh6Jz5.7a', 'ADMIN', NOW(), NOW()),
(uuid_generate_v4(), 'user', '$2a$10$hsz6D5eswZc1JlfIxurUEOuWWbbwooAXe0RhlvAsTKQzhDCuRZkKa', 'USER', NOW(), NOW());

-- 1. Employees
-- Columns: id, name, position, department, role, status, joined_date, location_id
INSERT INTO employees (id, name, position, department, role, status, joined_date, location_id) VALUES
(uuid_generate_v4(), 'Budi Santoso', 'Operator', 'Production', 'Operator', 'Active', '2023-01-01', 'Pit A'),
(uuid_generate_v4(), 'Siti Aminah', 'Admin', 'Office', 'Staff', 'Active', '2023-02-15', 'Office'),
(uuid_generate_v4(), 'Joko Widodo', 'Mechanic', 'Maintenance', 'Mechanic', 'Active', '2023-03-10', 'Workshop');

-- 2. Equipment
-- Columns: id, code, model, serial_number, engine_number, manufacture_year, hour_meter, kilometer, status, location_id
INSERT INTO equipment (id, code, model, serial_number, engine_number, manufacture_year, hour_meter, kilometer, status, location_id) VALUES
(uuid_generate_v4(), 'DT-001', 'HD785', 'SN-DT001', 'ENG-DT001', 2022, 1500.5, 5000.0, 'Operational', 'Pit A'),
(uuid_generate_v4(), 'EX-001', '320D', 'SN-EX001', 'ENG-EX001', 2022, 2500.0, 0.0, 'Operational', 'Pit A'),
(uuid_generate_v4(), 'DZ-001', 'D85', 'SN-DZ001', 'ENG-DZ001', 2021, 3000.2, 0.0, 'Maintenance', 'Workshop');

-- 3. Suppliers
-- Columns: id, name, contact_person, phone, email, address, category
INSERT INTO suppliers (id, name, contact_person, phone, email, address, category) VALUES
(uuid_generate_v4(), 'PT United Tractors', 'Andi', '021-1234567', 'sales@ut.com', 'Jakarta', 'SpareParts'),
(uuid_generate_v4(), 'PT Trakindo', 'Bambang', '021-7654321', 'sales@trakindo.com', 'Jakarta', 'Service');

-- 4. Pits (Already inserted successfully, but safe to ignore duplicates if any or just add new ones if needed. 
-- Since previous insert succeeded, we might skip or use ON CONFLICT DO NOTHING if supported, but standard SQL insert might fail on unique constraint.
-- Assuming previous insert succeeded, we skip Pits and Stockpiles to avoid errors, or delete first.)

-- DELETE FROM production_records;
-- DELETE FROM pits;
-- DELETE FROM stockpiles;

-- Re-insert Pits
INSERT INTO pits (id, code, name, location, status, seam) VALUES
(uuid_generate_v4(), 'PIT-C', 'Pit Charlie', 'Block C', 'Active', 'Seam C1')
ON CONFLICT (code) DO NOTHING;

-- Re-insert Stockpiles
INSERT INTO stockpiles (id, code, name, location, coal_type, current_stock, capacity) VALUES
(uuid_generate_v4(), 'SP-PORT', 'Port Stockpile', 'Port', 'Thermal', 20000.0, 50000.0)
ON CONFLICT (code) DO NOTHING;

-- 6. Production Records
-- Using subquery to get valid Pit ID
INSERT INTO production_records (id, date, pit_id, coal_type, quantity, shift, operator) 
SELECT uuid_generate_v4(), CURRENT_DATE, id, 'Thermal', 1500.0, 'Day', 'Budi Santoso' 
FROM pits LIMIT 1;
