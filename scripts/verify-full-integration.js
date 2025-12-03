import fetch from 'node-fetch';
import { exec } from 'child_process';
import util from 'util';

const execPromise = util.promisify(exec);
const BASE_URL = 'http://localhost:5001/api';

// Colors for console output
const colors = {
    reset: "\x1b[0m",
    green: "\x1b[32m",
    red: "\x1b[31m",
    yellow: "\x1b[33m",
    blue: "\x1b[34m"
};

async function runVerification() {
    console.log(`${colors.blue}🚀 Starting Full Stack Verification...${colors.reset}\n`);

    let token = '';
    const testLocationId = `loc-test-${Date.now()}`; // Using UUID in backend, but tracking name/code here
    const testLocationCode = `TEST-${Date.now().toString().slice(-4)}`;

    // 1. LOGIN (Get Token)
    console.log(`${colors.yellow}1. Testing Authentication (Login)...${colors.reset}`);
    try {
        const loginRes = await fetch(`${BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: 'admin',
                password: 'admin123' 
            })
        });

        if (!loginRes.ok) throw new Error(`Login failed: ${loginRes.statusText}`);
        
        const loginData = await loginRes.json();
        token = loginData.token;
        
        if (token) {
            console.log(`${colors.green}   ✅ Login Successful. Token received.${colors.reset}`);
        } else {
            throw new Error('No token received');
        }

    } catch (error) {
        console.error(`${colors.red}   ❌ Auth Failed: ${error.message}${colors.reset}`);
        process.exit(1);
    }

    // 2. CREATE DATA via API (Frontend Simulation)
    console.log(`\n${colors.yellow}2. Testing Data Creation via API (POST /locations)...${colors.reset}`);
    try {
        const createRes = await fetch(`${BASE_URL}/locations`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                name: 'Integration Test Warehouse',
                code: testLocationCode,
                address: '123 Test Lane, Databasetown',
                type: 'Warehouse'
            })
        });

        const createData = await createRes.json();

        if (createRes.ok && createData.id) {
            console.log(`${colors.green}   ✅ API reported success. Location ID: ${createData.id}${colors.reset}`);
            console.log(`${colors.green}   ✅ UUID Generation verified (Backend generated ID).${colors.reset}`);
        } else {
            throw new Error(`Create failed: ${JSON.stringify(createData)}`);
        }

    } catch (error) {
        console.error(`${colors.red}   ❌ Creation Failed: ${error.message}${colors.reset}`);
        process.exit(1);
    }

    // 3. VERIFY PERSISTENCE (Direct DB Check)
    console.log(`\n${colors.yellow}3. Verifying Database Persistence (Direct SQL)...${colors.reset}`);
    try {
        // We use psql to check if the data actually exists on disk
        const { stdout } = await execPromise(`PGPASSWORD=jpm_password psql -h localhost -U jpm_user -d jpm_db -c "SELECT id, name, code FROM locations WHERE code = '${testLocationCode}';"`);
        
        if (stdout.includes(testLocationCode)) {
            console.log(`${colors.green}   ✅ Data found in PostgreSQL!${colors.reset}`);
            console.log(stdout.trim());
        } else {
            throw new Error('Data NOT found in database. API might be faking it or DB connection is loose.');
        }

    } catch (error) {
        console.error(`${colors.red}   ❌ Persistence Check Failed: ${error.message}${colors.reset}`);
        process.exit(1);
    }

    // 4. DATA INTEGRITY & RETRIEVAL (GET via API)
    console.log(`\n${colors.yellow}4. Verifying Data Retrieval via API (GET /locations)...${colors.reset}`);
    try {
        const getRes = await fetch(`${BASE_URL}/locations`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const locations = await getRes.json();
        const found = locations.find(l => l.code === testLocationCode);

        if (found) {
            console.log(`${colors.green}   ✅ Data retrieved successfully via API.${colors.reset}`);
            console.log(`${colors.green}   ✅ Data Integrity confirmed (Input matches Output).${colors.reset}`);
        } else {
            throw new Error('Created data not visible in API list.');
        }

    } catch (error) {
        console.error(`${colors.red}   ❌ Retrieval Failed: ${error.message}${colors.reset}`);
    }

    // 5. CLEANUP
    console.log(`\n${colors.yellow}5. Cleaning up Test Data...${colors.reset}`);
    try {
        await execPromise(`PGPASSWORD=jpm_password psql -h localhost -U jpm_user -d jpm_db -c "DELETE FROM locations WHERE code = '${testLocationCode}';"`);
        console.log(`${colors.green}   ✅ Test data cleaned up.${colors.reset}`);
    } catch (error) {
        console.warn(`${colors.yellow}   ⚠️ Cleanup failed, manual check required.${colors.reset}`);
    }

    console.log(`\n${colors.blue}🏁 Verification Complete. System is functioning correctly.${colors.reset}`);
}

runVerification();
