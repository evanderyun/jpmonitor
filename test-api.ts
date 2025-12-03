// Quick API Test Script - Test PostgreSQL Backend
import api from './services/api';

async function testBackendAPI() {
    console.log('🧪 Testing PostgreSQL Backend API...\n');

    try {
        // Test 1: Login
        console.log('1️⃣ Testing Login...');
        const loginResult = await api.auth.login('admin', 'admin123');
        console.log('✅ Login successful:', loginResult.user);

        // Test 2: Get Spare Parts
        console.log('\n2️⃣ Testing Get Spare Parts...');
        const parts = await api.inventory.getParts();
        console.log(`✅ Fetched ${parts.length} spare parts`);

        // Test 3: Get Equipment
        console.log('\n3️⃣ Testing Get Equipment...');
        const equipment = await api.equipment.getEquipment();
        console.log(`✅ Fetched ${equipment.length} equipment`);

        // Test 4: Get Suppliers
        console.log('\n4️⃣ Testing Get Suppliers...');
        const suppliers = await api.suppliers.getSuppliers();
        console.log(`✅ Fetched ${suppliers.length} suppliers`);

        // Test 5: Get Employees
        console.log('\n5️⃣ Testing Get Employees...');
        const employees = await api.employees.getEmployees();
        console.log(`✅ Fetched ${employees.length} employees`);

        // Test 6: Get Analytics
        console.log('\n6️⃣ Testing Get Inventory Analytics...');
        const analytics = await api.inventory.getAnalytics();
        console.log('✅ Analytics:', analytics);

        console.log('\n✅ ALL TESTS PASSED! Backend API is working correctly.');
        console.log('\n📊 Summary:');
        console.log(`   - Spare Parts: ${parts.length}`);
        console.log(`   - Equipment: ${equipment.length}`);
        console.log(`   - Suppliers: ${suppliers.length}`);
        console.log(`   - Employees: ${employees.length}`);
        console.log(`   - Total Inventory Value: Rp ${analytics.totalValue?.toLocaleString() || 0}`);
        console.log(`   - Low Stock Items: ${analytics.lowStockCount}`);

    } catch (error: any) {
        console.error('❌ TEST FAILED:', error.message);
        console.error('Stack:', error.stack);
    }
}

// Run tests
if (typeof window !== 'undefined') {
    (window as any).testAPI = testBackendAPI;
    console.log('💡 API test function loaded. Run: testAPI()');
}

export default testBackendAPI;
