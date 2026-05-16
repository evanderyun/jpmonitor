# JpMonitor ERP — Technical Documentation

**Version:** 3.0
**Last Updated:** May 16, 2026
**Status:** Production Ready

---

## 1. System Architecture

The ERP system follows a **Modern Three-Tier Architecture** with AI integration:

```
┌─────────────────────────────────────────────────┐
│              Frontend (React 19)                 │
│           Vite 6 + TypeScript + Tailwind         │
│           React Router DOM (URL routing)         │
│           React.lazy code splitting              │
│           TanStack React Query (partial)         │
└──────────────────┬──────────────────────────────┘
                   │ REST API (JSON)
                   ▼
┌─────────────────────────────────────────────────┐
│        Backend — Spring Boot 3.4.4 / Java 21     │
│         Maven multi-module (3 modules)           │
│                                                  │
│  ├─ api/         REST controllers                │
│  ├─ domains/     Entities + Repositories + DTOs  │
│  └─ platform/    Security, JPA config, common     │
│                                                  │
│  Auth: JWT stateless, RBAC with JSONB permissions│
│  DB: PostgreSQL 16, Flyway migrations            │
└──────────────────┬──────────────────────────────┘
                   │ HTTP / SSE
                   ▼
┌─────────────────────────────────────────────────┐
│    Hermes Agent (AI Chat) — API Server :8642    │
│                                                  │
│  ─ MCP client → Spring Boot @Tool annotations   │
│  ─ EverOS memory (long-term memory)              │
│  ─ Skills + Web search (Firecrawl)               │
└─────────────────────────────────────────────────┘
```

### Key Tech Stack

| Layer | Technology | Notes |
|-------|-----------|-------|
| **Frontend** | React 19, TypeScript 5, Vite 6 | Tailwind CSS 4, recharts |
| **Backend** | Spring Boot 3.4.4, Java 21 | Virtual threads (Loom) |
| **AI Chat** | Hermes Agent (API Server) | MCP protocol with Spring AI |
| **Database** | PostgreSQL 16 | Schema in Flyway V1 |
| **Auth** | JWT + BCrypt + RBAC | JSONB permissions |
| **CI/CD** | GitHub Actions | 2 parallel jobs |

---

## 2. Backend Architecture

### Module Structure

```
backend-java/
├── pom.xml                          # Root - multi-module Maven
├── platform/
│   └── src/main/java/com/jpmonitor/platform/
│       ├── common/                  # BaseEntity, BaseImmutableEntity
│       ├── security/                # JwtUtils, SecurityConfig, JwtFilter
│       └── exception/               # ResourceNotFoundException, etc.
├── domains/
│   └── src/main/java/com/jpmonitor/domains/
│       ├── core/                    # User, Role, Project, Location, AuditLog
│       ├── fleet/                   # Equipment, WorkOrder, Maintenance, Fuel
│       ├── production/              # Pit, ProductionRecord, Stockpile, Hauling
│       ├── inventory/               # SparePart, InventoryTransaction
│       ├── finance/                 # CashAccount, Payment, AP
│       ├── logistics/               # GoodsShipment, ShipmentItem
│       ├── hr/                      # Employee
│       ├── hse/                     # Incident, Investigation
│       └── procurement/             # Supplier, ExternalService
└── api/
    └── src/main/java/com/jpmonitor/api/
        ├── controller/              # 17 REST controllers
        ├── dto/                     # ChatRequest/ChatResponse DTOs
        └── mcp/                     # @Tool classes for AI integration
```

### Database (PostgreSQL 16)

Managed via **Flyway** (11.x):
- `V1__initial_schema.sql` — 30+ tables with FKs, constraints, indexes, generated columns
- `V2__seed_data.sql` — 5 role levels, admin user, default project/locations

Key schema features:
- All UUID primary keys (auto-generated)
- Auto-generated computed columns (`stripping_ratio`, `total_cost`, `outstanding_amount`)
- JSONB for `roles.permissions` and `suppliers.api_auth_config`
- Audit trail via `audit_logs` table (immutable records)

### API Endpoints

| Module | Endpoints | Auth |
|--------|-----------|------|
| Auth | `POST /api/auth/login`, `GET /api/auth/me` | Public / JWT |
| Fleet | `GET/POST/PUT/DELETE /api/fleet/equipment`, work orders, daily logs, fuel, maintenance, mutations | JWT + RBAC |
| Inventory | `GET/POST/PUT/DELETE /api/inventory/parts`, transactions | JWT + RBAC |
| Production | `GET/POST/PUT /api/production/*` | JWT + RBAC |
| Finance | `GET/POST/PUT /api/finance/*` | JWT |
| Logistics | `GET/POST/PUT /api/logistics/*` | JWT |
| HR | `GET/POST/PUT /api/employees` | JWT |
| HSE | `GET/POST/PUT /api/hse/*` | JWT |
| Audit | `GET /api/audit?module=&page=&size=` | JWT |
| Chat | `POST /api/chat` → Hermes API Server | JWT |

### Security

| Feature | Implementation |
|---------|---------------|
| Auth | JWT stateless (24h expiry), BCrypt password hashing |
| RBAC | Role codes as Spring Security authorities + JSONB permissions |
| Rate limiting | 10 req/min/IP on login endpoint |
| CORS | Whitelist: localhost:3000, :3002, :5173, jpmonitor.duckdns.org |
| Headers | HSTS, Content Security Policy via Spring Security defaults |

---

## 3. Frontend Architecture

### Project Structure

```
src/ (project root)
├── components/
│   ├── ui/              # Reusable UI primitives (Card, Badge, Modal, etc.)
│   ├── FleetView.tsx    # Fleet management (1,003 lines - refactored)
│   ├── EquipmentList.tsx, MaintenanceView.tsx, FuelLogs.tsx, DailyLogs.tsx
│   ├── InventoryView.tsx# Inventory management (1,698 lines - refactored)
│   ├── InventoryDashboard.tsx, PartList.tsx, InventoryTransactions.tsx
│   ├── SupplierListEmbed.tsx
│   ├── DashboardView.tsx# Executive dashboard with KPIs
│   ├── ProductionView.tsx, EmployeeView.tsx, SupplierView.tsx
│   ├── MutationView.tsx, HSEView.tsx, LocationView.tsx
│   ├── DebtView.tsx, TimesheetView.tsx, AuditLogView.tsx
│   ├── Navigation.tsx   # Sidebar navigation with React Router NavLink
│   ├── AIChatWidget.tsx # Floating AI chat widget
│   └── LoginPage.tsx, ErrorBoundary.tsx, SearchableSelect.tsx
├── services/
│   ├── api.ts           # All REST API service objects (13 domains)
│   └── authStorage.ts   # JWT token persistence in localStorage
├── lib/
│   └── http.ts          # fetchJson wrapper (auth, dedup, error handling)
├── hooks/               # React hooks
├── utils/               # Helper functions (transformer: snake_case↔camelCase)
├── types.ts             # TypeScript interfaces (319 lines)
├── App.tsx              # Auth gate + React Router Routes
├── index.tsx            # ReactDOM.createRoot + BrowserRouter
└── config.ts            # Environment configuration
```

### Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **No state management library** | React context + prop drilling sufficient for ERP scope |
| **React.lazy code splitting** | Each view loaded on-demand (smaller initial bundle) |
| **Manual CRUD patterns** | Consistent REST calls instead of ORM-like abstraction |
| **snake_case↔camelCase transformer** | Bridge between PostgreSQL naming convention and TS |
| **AIChatWidget standalone** | Self-contained floating widget, not tied to views |

### AI Chat Widget

The chatbot (/api/chat) routes through:
```
AIChatWidget → POST /api/chat → ChatController (Java)
  → inject user context (name, role, company)
  → proxy to Hermes API Server :8642
  ↔ MCP tools for ERP operations
  ↔ EverOS memory for conversation history
```

---

## 4. AI Integration (Hermes Agent)

### Architecture

```
Hermes API Server (:8642)
├── MCP Client → Spring Boot MCP Server (SSE :8080/mcp)
│   ├── @Tool cari_equipment(code, status, location)
│   ├── @Tool buat_work_order(equipmentCode, description, priority)
│   ├── @Tool status_armada()
│   ├── @Tool cek_stok_sparepart(keyword)
│   ├── @Tool laporan_stok_menipis()
│   ├── @Tool rekap_produksi(pitCode, startDate, endDate)
│   ├── @Tool cari_user(keyword)
│   └── @Tool lokasi_project(keyword)
├── EverOS Memory (long-term)
├── Skills (self-evolving)
└── Firecrawl (web search)
```

### Setup

```bash
# .env
API_SERVER_ENABLED=true
API_SERVER_KEY=hermes-jpmonitor-dev
API_SERVER_CORS_ORIGINS=http://localhost:3002

# Start
hermes gateway
```

---

## 5. Deployment

### Docker Compose

```yaml
services:
  frontend:   # nginx + built files
  backend:    # Spring Boot JAR
  database:   # PostgreSQL 16
```

### Prerequisites

- Java 21 (Temurin)
- Node.js 20
- PostgreSQL 16
- Hermes Agent (for AI chat)

### Build & Run

```bash
# Backend
cd backend-java && mvn package -pl api -am -DskipTests

# Frontend
npm run build

# Docker
docker compose up -d
```

---

## 6. CI/CD Pipeline

GitHub Actions workflow (`.github/workflows/ci.yml`):

| Job | Steps | Triggers |
|-----|-------|----------|
| **Backend** | Java 21 setup, Maven compile, test (31 tests), package, Docker build | Push to main, PR |
| **Frontend** | Node 20 setup, npm ci, lint, test (16 tests, 1 skipped), build, Docker build | Push to main, PR |

---

## 7. Testing

| Layer | Framework | Tests | Notes |
|-------|-----------|-------|-------|
| Backend | JUnit 5 + Mockito | 31 tests (domain services) | H2 in PostgreSQL mode for integration |
| Frontend | Vitest + Testing Library | 16 tests + 1 skipped | jsdom environment, static rendering |

### Running Tests

```bash
# Backend
cd backend-java && mvn test -pl domains -am

# Frontend
npm test
```
