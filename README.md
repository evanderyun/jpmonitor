<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://github.com/user-attachments/assets/0aa67016-6eaf-458a-adb2-6e31a0763ed6" />
</div>

# JpMonitor

ERP system for mining operations — fleet, inventory, production, HR, and HSE management.

## Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 21, Spring Boot 3, Virtual Threads |
| **Frontend** | React 19, TypeScript, Vite, Tailwind CSS v4 |
| **Database** | PostgreSQL 16, Flyway migrations |
| **Auth** | JWT (JJWT), Spring Security, role-based (RBAC) |
| **Deploy** | Docker Compose, Nginx reverse proxy |
| **AI** | AI Chat Widget via Hestia agent framework |

## Features

- 🚛 **Fleet Management** — Equipment tracking, maintenance records, work orders, fuel logs
- 📦 **Inventory** — Spare parts, inventory transactions, stock control
- ⛏️ **Production** — Daily production records, stockpile, pit, coal quality
- 👥 **HR** — Employee records, timesheets
- 🚚 **Logistics** — Goods shipments, shipment items
- 🛒 **Procurement** — Suppliers, external services, price history
- 🛡️ **HSE** — Incident reporting, investigations
- 💰 **Finance** — Accounts payable, cash accounts, cost categories
- 📊 **Dashboard** — Fleet stats, inventory alerts, production overview
- 🤖 **AI Chat** — Embedded agent for queries and assistance
- 🔒 **Audit Trail** — Full transaction logging
- 🌙 **Dark Mode** — Full dark mode support

## Quick Start

### Prerequisites

- Docker + Docker Compose
- Java 21 (for local dev)
- Node.js 18+ (for frontend dev)

### Docker (recommended)

```bash
# Clone and start
git clone https://github.com/evanderyun/jpmonitor.git
cd jpmonitor

# Configure environment
cp .env.example .env
# Edit .env — set DB_PASSWORD and JWT_SECRET

# Start all services
docker compose up -d
```

Services:
- **Frontend** → http://localhost:3002
- **Backend API** → http://localhost:8080/api
- **PostgreSQL** → localhost:5436

### Local Development

```bash
# Backend
cd backend-java
mvn spring-boot:run

# Frontend
npm install
npm run dev
```

## Environment Variables

```bash
DB_HOST=jpmonitor-postgres
DB_PORT=5432
DB_NAME=jpmonitor_db
DB_USER=jpmonitor_user
DB_PASSWORD=CHANGE_ME
JWT_SECRET=CHANGE_ME_AT_LEAST_32_CHARS
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

Generate JWT secret: `openssl rand -base64 32`

## Project Structure

```
jpmonitor/
├── backend-java/
│   ├── api/          # REST controllers + config
│   ├── domains/      # Business logic + entities
│   └── platform/     # Shared security, utils
├── components/       # React UI components
├── hestia-api/       # AI chat bridge
├── docker-compose.yml
└── nginx.conf
```

## Documentation

- [Architecture & Design](DESIGN.md)
- [Deployment Guide](DEPLOYMENT.md)
- [Technical Documentation](TECHNICAL_DOCUMENTATION.md)
- [Security Audit](SECURITY_AUDIT.md)

---

**Status**: 🚧 Active Development
