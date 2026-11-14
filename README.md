# PulseVenue — Offline-First Event Operations & Cashless Commerce Platform

**Tech Stack:** Java 21 (JDK `HttpServer` MVP · Spring Boot 3.3 target) · React 18 (Vite) · PostgreSQL 16  
**Completed Issues:** `ISS-01`, `ISS-02`, `ISS-03`, `ISS-05`, `ISS-06`, `ISS-07`, `ISS-08`, `ISS-10`, `ISS-11`, `ISS-12`, `ISS-13`, `ISS-14`, `ISS-15`, `ISS-17`, `ISS-19`, `ISS-25`

---

## 1. Project Concept & Barebone Simplification Strategy

**PulseVenue** is an offline-first event operations and cashless commerce platform running on standard consumer phones and tablets:
1. **Attendee Mobile Wallet (React):** Generates cryptographically signed, rotating QR codes (`HMAC-SHA256` for barebone MVP) and displays geofenced flash discounts.
2. **Bartender Tablet POS (React Offline-First):** Verifies QR signatures and balances **100% offline**, queues transactions in `localStorage`, and batch-syncs them when connectivity returns.
3. **Double-Entry Cloud Ledger (Java 21 + PostgreSQL):** Settles transaction batches atomically with idempotency (`UNIQUE(idempotency_key)`) so network retries never double-charge attendees.
4. **Uber H3 Crowd Telemetry & 80k Simulator (Java 21):** Aggregates attendee locations into H3 spatial zones, detects bottlenecks (`>= 85%` capacity), and triggers geofenced flash discounts at nearby under-utilized bars.

👉 See **[`docs/pm/SIMPLIFICATION_ROADMAP.md`](docs/pm/SIMPLIFICATION_ROADMAP.md)** for how each subsystem is scoped for **Sprint 1–2 (Barebone Working MVP)** vs **Sprint 3–4 (Heavy Extensions)**.

---

## 2. Repository Structure & Role Ownership (7 Team Members / 9 Roles)

| Role | Status | Owned Directories & Key Files | Issue / PR Plan |
| :--- | :---: | :--- | :--- |
| **Project Manager & DevOps** | ✅ **Initialized** | `.github/workflows/`, `docker-compose.yml`, `docs/pm/`, `scripts/` | `ISS-01` (✅), `ISS-18`, `ISS-27` |
| **Software Architect** | ✅ **Initialized** | `docs/architecture/ARCHITECTURE.md`, `Models.java` (Contracts) | `ISS-03` (✅), `ISS-20` |
| **Backend Developers (Java 21)** | 🟢 **In Progress** | `backend/src/main/java/com/pulsevenue/service/`, `simulator/` | `ISS-05` (✅), `ISS-10` (✅), `ISS-11` (✅), `ISS-12` (✅), `ISS-19` (✅), `ISS-24` |
| **Frontend Developer (React)** | 🟢 **In Progress** | `frontend/src/App.jsx`, `frontend/src/components/` | `ISS-06` (✅), `ISS-13` (✅), `ISS-14` (✅), `ISS-22` |
| **Database Engineer (PostgreSQL)** | 📋 Open for PRs | `database/schema.sql`, `database/seed.sql`, `database/ER_DIAGRAM.md` | `ISS-04`, `ISS-10` (✅), `ISS-21` |
| **UI Designer** | 🟢 **In Progress** | `docs/ui/UI_DESIGN_SPEC.md`, `docs/ui/ISS-07_DESIGN_FOUNDATIONS.md`, `frontend/src/styles.css` | `ISS-07` (✅), `ISS-16`, `ISS-26` |
| **Business Analyst (BA)** | 🟢 **In Progress** | `docs/ba/REQUIREMENTS_AND_USE_CASES.md` | `ISS-02` (✅), `ISS-15` (✅), `ISS-26` |
| **QA / Tester** | ✅ **Done** | `docs/qa/TEST_PLAN.md`, `backend/src/test/java/com/pulsevenue/` | `ISS-08` (✅), `ISS-17` (✅), `ISS-25` (✅) |
| **Technical Writer** | 📋 Open for PRs | `docs/technical_writing/USER_AND_API_GUIDE.md`, `README.md` | `ISS-09`, `ISS-23`, `ISS-27` |

---

## 3. Quick Start & Docker Demo

### Run the Full-Stack Demo in Docker (One Command)
```bash
bash scripts/demo.sh          # Builds & starts Postgres + Java 21 + React Nginx (:3000)
bash scripts/demo.sh smoke    # Runs live E2E API, idempotency, 80k simulator & DB walkthrough
bash scripts/demo.sh down     # Stops containers and resets demo state
```

### Verify the Build & CI Checks Locally (Runs in 2 seconds)
```bash
bash scripts/verify_skeleton.sh
```

---

## 4. Documentation Index

| Area | Document |
| :--- | :--- |
| **Project Management** | [`docs/pm/SIMPLIFICATION_ROADMAP.md`](docs/pm/SIMPLIFICATION_ROADMAP.md) · [`docs/pm/TEAM_ROLES_AND_PLANS.md`](docs/pm/TEAM_ROLES_AND_PLANS.md) · [`docs/pm/ISSUES_BACKLOG.md`](docs/pm/ISSUES_BACKLOG.md) |
| **Architecture & Contracts** | [`docs/architecture/ARCHITECTURE.md`](docs/architecture/ARCHITECTURE.md) |
| **Business Analysis** | [`docs/ba/REQUIREMENTS_AND_USE_CASES.md`](docs/ba/REQUIREMENTS_AND_USE_CASES.md) |
| **UI & Design System** | [`docs/ui/ISS-07_DESIGN_FOUNDATIONS.md`](docs/ui/ISS-07_DESIGN_FOUNDATIONS.md) · [`docs/ui/UI_DESIGN_SPEC.md`](docs/ui/UI_DESIGN_SPEC.md) |
| **Database** | [`database/ER_DIAGRAM.md`](database/ER_DIAGRAM.md) · [`database/schema.sql`](database/schema.sql) |
| **QA & Testing** | [`docs/qa/TEST_PLAN.md`](docs/qa/TEST_PLAN.md) |
| **Setup & API Guide** | [`docs/technical_writing/USER_AND_API_GUIDE.md`](docs/technical_writing/USER_AND_API_GUIDE.md) |
