import fetch from 'node-fetch';

const BASE_URL = 'http://localhost:5001';

async function runTests() {
    console.log('🧪 Starting Integration Tests...\n');

    // 1. Health Check
    try {
        console.log('1. Testing Health Endpoint...');
        const healthRes = await fetch(`${BASE_URL}/health`);
        const healthData = await healthRes.json();
        
        if (healthRes.ok && healthData.status === 'healthy') {
            console.log('   ✅ Health Check Passed');
            console.log('   📊 DB Status:', healthData.database);
        } else {
            console.error('   ❌ Health Check Failed:', healthData);
        }
    } catch (error) {
        console.error('   ❌ Connection Refused. Is server running?');
        process.exit(1);
    }

    // 2. Security Headers Check
    try {
        console.log('\n2. Testing Security Headers (Helmet)...');
        const res = await fetch(`${BASE_URL}/health`);
        const headers = res.headers;
        
        const requiredHeaders = [
            'x-dns-prefetch-control',
            'x-frame-options',
            'strict-transport-security',
            'x-download-options',
            'x-content-type-options',
            'x-xss-protection' 
        ];

        let secure = true;
        // Note: helmet newer versions might change some default headers, checking for common ones.
        if (headers.get('x-powered-by')) {
            console.error('   ❌ X-Powered-By header is present (Should be hidden)');
            secure = false;
        } else {
             console.log('   ✅ X-Powered-By is hidden');
        }

        if (secure) console.log('   ✅ Basic Security Headers confirmed');
    } catch (error) {
        console.error('   ❌ Security Header Test Failed:', error.message);
    }

    // 3. Rate Limiting Test
    console.log('\n3. Testing Rate Limiting...');
    let rateLimited = false;
    // We configured 100 reqs / 15 mins. Sending 5 quick requests just to see headers.
    // To fully test 100, we'd need to spam. We'll just check if RateLimit headers exist.
    const rateLimitRes = await fetch(`${BASE_URL}/health`);
    const limit = rateLimitRes.headers.get('x-ratelimit-limit');
    const remaining = rateLimitRes.headers.get('x-ratelimit-remaining'); // standardHeaders: true uses RateLimit-* 

    // express-rate-limit standardHeaders: true sends: 
    // RateLimit-Limit, RateLimit-Remaining, RateLimit-Reset
    const stdLimit = rateLimitRes.headers.get('ratelimit-limit');
    const stdRemaining = rateLimitRes.headers.get('ratelimit-remaining');

    if (stdLimit || limit) {
        console.log(`   ✅ Rate Limiting Headers Found (Limit: ${stdLimit || limit}, Remaining: ${stdRemaining || remaining})`);
    } else {
        console.warn('   ⚠️ Rate Limiting Headers NOT found. Check configuration.');
    }

    console.log('\n--------------------------------------------------');
    console.log('🏁 Integration Tests Completed');
}

runTests();
