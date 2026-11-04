# PulseVenue — Offline-First Event Operations & Cashless Commerce Platform

**Tech Stack:** Java 21 (Spring Boot) · React 18 (Vite) · PostgreSQL 16  
**Initialized Roles:** Project Manager / DevOps (`ISS-01`) & Software Architect (`ISS-03`)

---

## 1. Project Concept & Barebone Simplification Strategy

**PulseVenue** is an offline-first event operations and cashless commerce platform running on standard consumer phones and tablets:
1. **Attendee Mobile Wallet (React):** Generates cryptographically signed, rotating QR codes (`HMAC-SHA256` for barebone MVP) and displays geofenced flash discounts.
2. **Bartender Tablet POS (React Offline-First):** Verifies QR signatures and balances **100% offline**, queues transactions in `localStorage`, and batch-syncs them when connectivity returns.
3. **Double-Entry Cloud Ledger (Java 21 + PostgreSQL):** Settles transaction batches atomically with idempotency (`UNIQUE(idempotency_key)`) so network retries never double-charge attendees.
4. **Uber H3 Crowd Telemetry & 80k Simulator (Java 21):** Aggregates attendee locations into H3 spatial zones, detects dangerous bottlenecks (`>= 85%` capacity), and automatically triggers geofenced flash discounts at nearby under-utilized bars.

👉 See **[`docs/pm/SIMPLIFICATION_ROADMAP.md`](docs/pm/SIMPLIFICATION_ROADMAP.md)** for how each subsystem is simplified for **Sprint 1–2 (Barebone Working MVP)** vs **Sprint 3–4 (Heavy Extensions)**.

---

## 2. Repository Structure & Role Ownership (7 Team Members / 9 Roles)

| Role | Status | Owned Directories & Skeletal Files | Issue / PR Plan |
| :--- | :---: | :--- | :--- |
| **Project Manager & DevOps** | ✅ **Done** | `.github/workflows/ci.yml`, `docker-compose.yml`, `docs/pm/`, `scripts/` | `ISS-01`, `ISS-18`, `ISS-27` |
| **Software Architect** | ✅ **Done** | `docs/architecture/ARCHITECTURE.md`, `Models.java` (Contracts) | `ISS-03`, `ISS-20` |
| **Backend Developers (Java 21)** | 📋 Open for PRs | `backend/src/main/java/com/pulsevenue/service/`, `simulator/` | `ISS-05`, `ISS-10`, `ISS-11`, `ISS-12`, `ISS-19`, `ISS-24` |
| **Frontend Developer (React)** | 📋 Open for PRs | `frontend/src/App.jsx`, `frontend/src/components/` | `ISS-06`, `ISS-13`, `ISS-14`, `ISS-22` |
| **Database Engineer (PostgreSQL)** | 📋 Open for PRs | `database/schema.sql`, `database/seed.sql`, `database/ER_DIAGRAM.md` | `ISS-04`, `ISS-10`, `ISS-21` |
| **UI Designer** | 📋 Open for PRs | `docs/ui/UI_DESIGN_SPEC.md`, `frontend/src/styles.css` | `ISS-07`, `ISS-16`, `ISS-26` |
| **Business Analyst (BA)** | 📋 Open for PRs | `docs/ba/REQUIREMENTS_AND_USE_CASES.md` | `ISS-02`, `ISS-15`, `ISS-26` |
| **QA / Tester** | 📋 Open for PRs | `docs/qa/TEST_PLAN.md`, `backend/src/test/java/com/pulsevenue/PulseVenueTest.java` | `ISS-08`, `ISS-17`, `ISS-25` |
| **Technical Writer** | 📋 Open for PRs | `docs/technical_writing/USER_AND_API_GUIDE.md`, `README.md` | `ISS-09`, `ISS-23`, `ISS-27` |

---

## 3. Quick Verification & PR Workflow

### Verify the Skeleton Locally (Runs in 2 seconds)
```bash
bash scripts/verify_skeleton.sh
```

### Push All Role Issues to GitHub Issues (Project Manager)
```bash
bash scripts/create_github_issues.sh
```

### Key Project Manager & Architect Documents
- **[Simplification & Sprint Roadmap](docs/pm/SIMPLIFICATION_ROADMAP.md)**
- **[Team Roles & PR Implementation Plans](docs/pm/TEAM_ROLES_AND_PLANS.md)**
- **[Full Issues Backlog (`ISS-01`..`ISS-27`)](docs/pm/ISSUES_BACKLOG.md)**
- **[Software Architecture (C4 Diagrams, Sequence Diagrams & Frozen REST Contracts)](docs/architecture/ARCHITECTURE.md)**
