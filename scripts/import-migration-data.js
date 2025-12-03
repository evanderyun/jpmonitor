// =================================================================
// DATA MIGRATION IMPORT SCRIPT
// PT JAVA PERSADA MANDIRI ERP - Import to PostgreSQL
// =================================================================

const fs = require('fs');
const path = require('path');
const { query } = require('../server/db/connection');

async function importData() {
    console.log('🚀 Starting Data Import to PostgreSQL...\n');

    // Read migration file
    const migrationFile = process.argv[2] || './migration-data.json';
    const filePath = path.resolve(migrationFile);

    if (!fs.existsSync(filePath)) {
        console.error(`❌ Migration file not found: ${filePath}`);
        console.log('Usage: node scripts/import-migration-data.js <path-to-json>');
        process.exit(1);
    }

    const rawData = fs.readFileSync(filePath, 'utf8');
    const data = JSON.parse(rawData);

    console.log(`📁 Loaded file: ${migrationFile}`);
    console.log(`📊 Total Records:`, data.total_records);
    console.log('');

    let totalImported = 0;

    try {
        // Import Locations first (no dependencies)
        console.log('📍 Importing Locations...');
        for (const loc of data.locations || []) {
            await query(`
                INSERT INTO locations (id, name, code, address, type, created_at)
                VALUES ($1, $2, $3, $4, $5, NOW())
                ON CONFLICT (id) DO NOTHING
            `, [loc.id, loc.name, loc.code || null, loc.address || null, loc.type || 'Site']);
            totalImported++;
        }
        console.log(`✅ Imported ${data.locations.length} locations\n`);

        // Import Suppliers
        console.log('🏢 Importing Suppliers...');
        for (const sup of data.suppliers || []) {
            await query(`
                INSERT INTO suppliers (id, name, contact_person, phone, email, address, city, country, payment_terms, rating, created_at)
                VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, NOW())
                ON CONFLICT (id) DO NOTHING
            `, [
                sup.id, sup.name, sup.contactPerson || null, sup.phone || null,
                sup.email || null, sup.address || null, sup.city || null,
                sup.country || 'Indonesia', sup.paymentTerms || null, sup.rating || 3
            ]);
            totalImported++;
        }
        console.log(`✅ Imported ${data.suppliers.length} suppliers\n`);

        // Import Employees
        console.log('👷 Importing Employees...');
        for (const emp of data.employees || []) {
            await query(`
                INSERT INTO employees (id, name, position, department, phone, email, hire_date, status, created_at)
                VALUES ($1, $2, $3, $4, $5, $6, $7, $8, NOW())
                ON CONFLICT (id) DO NOTHING
            `, [
                emp.id, emp.name, emp.position || null, emp.department || 'Production',
                emp.phone || null, emp.email || null, emp.joinedDate || emp.hireDate || null,
                emp.status || 'Active'
            ]);
            totalImported++;
        }
        console.log(`✅ Imported ${data.employees.length} employees\n`);

        // Import Equipment
        console.log('🚜 Importing Equipment...');
        for (const eq of data.equipment || []) {
            await query(`
                INSERT INTO equipment (
                    id, code, name, type, manufacturer, model, serial_number, year,
                    status, location, assigned_to, purchase_date, purchase_cost, created_at
                ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, NOW())
                ON CONFLICT (code) DO NOTHING
            `, [
                eq.id, eq.code, eq.name, eq.type || null, eq.manufacturer || null,
                eq.model || null, eq.serialNumber || null, eq.year || eq.manufactureYear || null,
                eq.status || 'Operational', eq.location || null, eq.assignedTo || null,
                eq.purchaseDate || null, eq.purchaseCost || null
            ]);
            totalImported++;
        }
        console.log(`✅ Imported ${data.equipment.length} equipment\n`);

        console.log('='.repeat(60));
        console.log(`🎉 Migration Complete!`);
        console.log(`📊 Total Records Imported: ${totalImported}`);
        console.log('='.repeat(60));

    } catch (error) {
        console.error('\n❌ Migration Error:', error.message);
        console.error('Stack:', error.stack);
        process.exit(1);
    }

    process.exit(0);
}

// Run if called directly
if (require.main === module) {
    importData();
}

module.exports = { importData };
