# Security Audit & Readiness Report - JpMonitor ERP

**Date:** November 29, 2025
**Auditor:** Gemini Agent

## Executive Summary
The project is a React (Vite) frontend with an Express/PostgreSQL backend. While the codebase demonstrates some good security practices (parameterized queries, JWT auth, role-based access), it is **not yet production-ready**. Several critical vulnerabilities and configuration issues need to be addressed before deployment.

## Critical Vulnerabilities (High Priority)

### 1. Secrets Management & API Keys
- **Risk:** `GEMINI_API_KEY` is exposed to the frontend via `vite.config.ts` (`process.env.GEMINI_API_KEY`).
- **Impact:** Anyone with access to the frontend (browser) can extract this key and misuse your Gemini quota, potentially incurring costs.
- **Fix:** Move all 3rd-party API calls requiring secrets to the **backend**. The frontend should call your backend, which then calls Gemini.

### 2. Hardcoded Backend URL
- **Risk:** `API_BASE_URL` is hardcoded to `http://localhost:5001/api` in `services/api.ts`.
- **Impact:** The application will fail in production unless the domain happens to be `localhost`. Rebuilding the app is required to change the URL.
- **Fix:** Use environment variables (e.g., `import.meta.env.VITE_API_URL`) to configure the URL dynamically.

### 3. Insecure Session Storage (XSS Risk)
- **Risk:** JWT tokens are stored in `localStorage` (`services/api.ts`).
- **Impact:** If the application has any Cross-Site Scripting (XSS) vulnerability (e.g., via a compromised npm package or unsanitized input), the attacker can steal the token and hijack user sessions.
- **Fix:** Store tokens in **HttpOnly, Secure cookies**.

### 4. Missing Security Headers
- **Risk:** The Express server (`server/index.js`) lacks standard security headers.
- **Impact:** Vulnerable to clickjacking, sniffing, and other common web attacks.
- **Fix:** Install and use `helmet` middleware.

### 5. No Rate Limiting
- **Risk:** No rate limiting on API endpoints.
- **Impact:** Vulnerable to brute-force attacks (especially on `/api/auth/login`) and Denial of Service (DoS).
- **Fix:** Implement `express-rate-limit`.

## Architectural & Code Quality Issues

### 1. Database Schema Conflict
- **Observation:** `server/db/schema.sql` contains two conflicting definitions for the `maintenance_records` table. This will cause deployment errors or data model inconsistency.

### 2. Weak ID Generation
- **Observation:** The backend uses `Date.now()` (e.g., `user-${Date.now()}`) for ID generation.
- **Risk:** High probability of ID collisions in a production environment with concurrent users.
- **Fix:** Use `uuid` (v4) or database `SERIAL`/`UUID` types.

### 3. Logging
- **Observation:** Uses `console.log` extensively.
- **Fix:** Use a structured logger (e.g., `winston` or `pino`) for production to manage log levels and avoid leaking sensitive info.

## Readiness Score: 4/10
*The application works functionally for development but requires significant hardening for production.*

## Recommended Action Plan
1.  **Refactor Config:** externalize `API_BASE_URL` and secrets.
2.  **Harden Server:** Add `helmet`, `cors` strict options, and `rate-limit`.
3.  **Fix Schema:** Resolve `maintenance_records` duplication.
4.  **Secure Auth:** Move to HttpOnly cookies (requires backend and frontend changes).
5.  **Proxy API:** Create a backend route for Gemini calls to hide the key.
