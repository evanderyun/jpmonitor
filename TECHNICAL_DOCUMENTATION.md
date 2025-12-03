# PT Java Persada Mandiri ERP - Technical Documentation

**Version:** 2.0
**Last Updated:** November 29, 2025
**Status:** Production Ready (Security Hardened)

---

## 1. System Architecture

The ERP system follows a classic **Three-Tier Architecture**:

*   **Frontend (Presentation Layer):**
    *   **Technology:** React.js (Vite)
    *   **Language:** TypeScript
    *   **Communication:** REST API via `fetch` (abstracted in `services/api.ts`)
    *   **Configuration:** Environment variables (`.env.local`) for API URLs.

*   **Backend (Logic Layer):**
    *   **Technology:** Node.js with Express
    *   **Language:** JavaScript (CommonJS)
    *   **Security:** `helmet` (Headers), `cors` (Origin Control), `express-rate-limit` (DDoS Protection).
    *   **Auth:** JWT (JSON Web Tokens) for stateless authentication.
    *   **Role Management:** RBAC (Role-Based Access Control) with granular permissions.

*   **Database (Data Layer):**
    *   **Technology:** PostgreSQL 14+
    *   **Schema:** Relational Normalized Schema (3NF)
    *   **Integrity:** Foreign Keys, Check Constraints (Non-negative stock), ACID Transactions.

---

## 2. Security Implementation

The system has been audited and hardened against common OWASP vulnerabilities.

### 2.1. Middleware Stack
| Middleware | Purpose | Configuration |
| :--- | :--- | :--- |
| **Helmet** | Secure HTTP Headers | Hides `X-Powered-By`, sets `Strict-Transport-Security`, etc. |
| **CORS** | Cross-Origin Resource Sharing | Restricted to `FRONTEND_URL` only. |
| **Rate Limit** | Brute-force Protection | Max 100 requests / 15 mins per IP. |
| **XSS Clean** | Input Sanitization | Strips malicious scripts from request body. |
| **HPP** | Parameter Pollution | Prevents duplicate query parameter attacks. |

### 2.2. Data Protection
*   **UUIDs:** Used for all IDs (`v4`) instead of predictable auto-increment integers or `Date.now()`.
*   **Secret Management:** All keys (`JWT_SECRET`, `GEMINI_API_KEY`, `DB_PASSWORD`) are moved to `.env`.
*   **API Proxy:** Frontend never calls 3rd party APIs (Gemini) directly; it requests the Backend, which acts as a secure proxy.

---

## 3. Database Schema (Key Modules)

### 3.1. Inventory & Supply Chain
*   **`spare_parts`**: Master catalog.
    *   *Constraint:* `current_stock >= 0` (Prevents negative inventory).
    *   *Relation:* Linked to `suppliers` and `locations`.
*   **`inventory_transactions`**: Immutable ledger of stock movements.
    *   *Integrity:* `ON DELETE RESTRICT` ensures used parts cannot be deleted from catalog.

### 3.2. Maintenance & Engineering
*   **`equipment`**: Fleet master data.
    *   *Constraint:* `hour_meter >= 0`.
*   **`maintenance_records`**: Unified service history.
    *   *Feature:* Supports **Hour Meter Reset** logic (`hm_reset_occurred`) for engine replacements.
    *   *Relation:* Links to `work_orders`, `employees` (mechanics), and `suppliers` (external service).

### 3.3. Human Resources
*   **`users`**: System access.
*   **`roles`**: Permission sets (`admin`, `manager`, `mechanic`).
*   **`employees`**: HR data, linked to users but separate entities.

---

## 4. Setup & Deployment

### 4.1. Prerequisites
*   Node.js v18+
*   PostgreSQL v14+

### 4.2. Environment Variables
Create a `.env` file in the `server/` directory:
```env
PORT=5001
DB_HOST=localhost
DB_USER=jpm_user
DB_PASSWORD=your_secure_password
DB_NAME=jpm_db
JWT_SECRET=complex_secret_key
FRONTEND_URL=http://localhost:3000
GEMINI_API_KEY=your_key_here
```

### 4.3. Database Initialization
Run the schema script to create tables and default admin user:
```bash
psql -h localhost -U jpm_user -d jpm_db -f server/db/schema.sql
```

### 4.4. Running the System
**Backend:**
```bash
cd server
npm install
npm start
```

**Frontend:**
```bash
# In root directory
npm install
npm run dev
```

---

## 5. API Reference (Examples)

### Auth
*   `POST /api/auth/login`: Authenticate and receive JWT.
*   `GET /api/auth/me`: Get current user profile.

### Inventory
*   `GET /api/inventory/parts`: List spare parts.
*   `POST /api/inventory/transactions`: Record usage or purchase.

### Gemini AI (Proxy)
*   `POST /api/gemini/generate`: Securely generate insights using backend key.
    *   *Body:* `{ "prompt": "Analyze mining data..." }`

---

*End of Documentation*
 
---

## 6. Frontend Architecture Updates

- Configuration via environment: `VITE_API_BASE_URL` controls backend URL at build/runtime.
- HTTP abstraction: centralized `fetchJson` adds Authorization, handles errors, and cancels stale GET requests.
- Error handling: root `ErrorBoundary` prevents full-app crash and shows a safe fallback.
- Performance: `React.lazy` + `Suspense` for large views to reduce initial bundle; caching prepared via Query Client Provider.
- Accessibility: interactive widgets (e.g., combobox) include ARIA roles and keyboard support.
- Testing: `vitest` + Testing Library for UI smoke tests; add more tests as modules grow.
