# Team Roles, PR Workflow & Implementation Plans (Java 21 + React + PostgreSQL)

**Completed Setup:** Project Manager / DevOps & Software Architect  
**Team Size:** 7 Members covering 9 Role Areas

---

## 1. Role Allocation Across the 7 Team Members

| Member Slot | Primary Role | Secondary / Paired Role | Status | Directories Owned for Pull Requests |
| :--- | :--- | :--- | :---: | :--- |
| **Member 1 (You)** | **Project Manager & DevOps** | **Software Architect** | ✅ **Done** | `.github/`, `docker-compose.yml`, `docs/pm/`, `docs/architecture/`, `scripts/` |
| **Member 2** | **Backend Developer 1** | *(Crypto QR & Ledger Sync)* | ✅ **Done (`ISS-05`, `ISS-10`, `ISS-11`)** | `backend/src/main/java/com/pulsevenue/service/`, `PulseVenueApplication.java` |
| **Member 3** | **Backend Developer 2** | *(H3 Telemetry & 80k Simulator)* | 🟢 **In Progress (`ISS-12`, `ISS-19` ✅)** | `backend/src/main/java/com/pulsevenue/simulator/`, `service/` |
| **Member 4** | **Database Engineer** | *(PostgreSQL Schema & Views)* | 📋 Ready for PRs | `database/` |
| **Member 5** | **Frontend Developer** | *(React Views, State & Offline POS)* | 📋 Ready for PRs | `frontend/src/components/`, `frontend/src/api/`, `frontend/src/App.jsx` |
| **Member 6** | **UI Designer** | **Business Analyst (BA)** | 🟢 **In Progress (`ISS-02`, `ISS-07`, `ISS-15` ✅)** | `docs/ui/`, `docs/ba/`, `frontend/src/styles.css` |
| **Member 7** | **QA / Tester** | **Technical Writer** | 📋 Ready for PRs | `backend/src/test/`, `frontend/src/__tests__/`, `docs/qa/`, `docs/technical_writing/` |

---

## 2. Git & Pull Request Workflow for the Team

1. **Pick an Issue** from `docs/pm/ISSUES_BACKLOG.md` (or the GitHub Projects board).
2. **Create a Feature Branch** named after the issue ID and role:
   ```bash
   git checkout -b feat/ISS-05-qr-crypto-service
   ```
3. **Implement only the scoped task** (replace the `TODO (ISS-xx)` stub in the assigned folder).
4. **Run local checks** before pushing:
   - Backend (Java 21): `mvn test` (or `./scripts/verify_skeleton.sh`)
   - Frontend (React): `cd frontend && npm run build`
5. **Open a Pull Request** against `main` using `.github/PULL_REQUEST_TEMPLATE.md` and link `Closes #X`.
6. **CI/CD Gate:** GitHub Actions (`.github/workflows/ci.yml`) automatically compiles Java 21, runs tests, validates SQL files, and builds the React frontend. At least 1 reviewer (PM/Architect) approves before merge.

---

## 3. Implementation Plan for Each Teammate Role

### Role A: Backend Developers (Java 21)
- **What the Architect already provided:**
  - `backend/pom.xml` (Java 21 Maven configuration)
  - `backend/src/main/java/com/pulsevenue/model/Models.java` (Frozen Java 21 `record` DTOs matching the REST API contract in `docs/architecture/ARCHITECTURE.md`)
  - `backend/src/main/java/com/pulsevenue/service/PulseVenueService.java`
- **PR Implementation Plan:**
  - ✅ **PR 1 (`ISS-05` — Done):** Implemented `generateQrToken()` and `verifyQrTokenOffline()` using `javax.crypto.Mac` (`HmacSHA256`) and constant-time signature comparison (`MessageDigest.isEqual`).
  - ✅ **PR 2 (`ISS-10` & `ISS-11` — Done):** Implemented `syncOfflineBatch()` with idempotency check (`idempotencyKey`) and exposed REST endpoints (`/api/v1/state`, `/api/v1/qr/generate`, `/api/v1/qr/verify-offline`, `/api/v1/transactions/sync`).
  - ✅ **PR 3 (`ISS-12` — Done):** Implemented `updateZoneTelemetry()` in `PulseVenueService` to detect crowd bottlenecks (`occupancyRatio >= 0.85`) and create a 20% `FlashDiscount` for the nearest `NORMAL` bar.
  - 🟢 **PR 4 (`ISS-19` ✅ & `ISS-24` 📋):** Implemented the 80,000-agent tick simulator (`CrowdSimulator.java`); remaining stretch task is `com.uber:h3` coordinate indexing (`ISS-24`).

---

### Role B: Database Engineer (PostgreSQL / SQL)
- **What the Architect already provided:**
  - `docker-compose.yml` with PostgreSQL 16 service container
  - `database/schema.sql` and `database/seed.sql` starter skeletons
  - `database/ER_DIAGRAM.md` template
- **PR Implementation Plan:**
  - **PR 1 (`ISS-04` — Open):** Complete `database/schema.sql` (`wallets`, `ledger_entries` with `UNIQUE(idempotency_key)` and `CHECK (amount > 0)`, `zones`, `flash_discounts`) and `database/seed.sql`, plus complete the Mermaid ER diagram in `database/ER_DIAGRAM.md`.
  - ✅ **PR 2 (`ISS-10` — Done in Service Layer):** Double-entry batch settlement and idempotency verification implemented in `PulseVenueService.syncOfflineBatch()`.
  - **PR 3 (`ISS-21` — Open):** Add composite indexes (`idx_ledger_debit_created`, `idx_zones_status`) and a SQL view `v_ledger_reconciliation` verifying `SUM(debits) == SUM(credits)`.

---

### Role C: Frontend Developer (React 18 + Vite)
- **What the Architect already provided:**
  - `frontend/package.json`, `frontend/vite.config.js` (proxying `/api` to `http://localhost:8080`), `frontend/index.html`, `frontend/src/main.jsx`, and skeletal `frontend/src/App.jsx` with the 3 tab shells.
- **PR Implementation Plan:**
  - **PR 1 (`ISS-06` — Open):** Build the `AttendeeWallet` component displaying wallet balance, rotating QR token payload, countdown timer, and active flash discount banner.
  - **PR 2 (`ISS-13` — Open):** Build the `BartenderPos` component with an **Online / Offline toggle**, local QR verification, `localStorage` queue (`pulsevenue_pending_txs`), and "Sync Batch to Cloud Ledger" button.
  - **PR 3 (`ISS-14` & `ISS-22` — Open):** Build the `OrganizerDashboard` component displaying H3 zone occupancy cards, bottleneck badges, and the "Run 80,000-Agent Simulation Tick" trigger button + QR matrix SVG renderer.

---

### Role D: UI Designer
- **What the Architect already provided:**
  - `docs/ui/UI_DESIGN_SPEC.md` starter template and `frontend/src/styles.css` base CSS variables.
- **PR Implementation Plan:**
  - ✅ **PR 1 (`ISS-07` — Done):** Completed the color token table, typography scale, WCAG contrast checks in `docs/ui/ISS-07_DESIGN_FOUNDATIONS.md`, SVG brand logos, and 3-tab dark theme layout in `frontend/src/styles.css`.
  - **PR 2 (`ISS-16` — Open):** Style high-contrast status badges (`NORMAL` Green, `ELEVATED` Amber, `BOTTLENECK` Pulsing Red, and `OFFLINE POS MODE` banner), H3 heatmap cards, and POS keypad in `frontend/src/styles.css`.
  - **PR 3 (`ISS-26` — Open):** Mobile responsiveness (`<480px` phone vs `1024px` tablet) and final accessibility contrast audit.

---

### Role E: Business Analyst (BA)
- **What the Architect already provided:**
  - `docs/ba/REQUIREMENTS_AND_USE_CASES.md` structured template.
- **PR Implementation Plan:**
  - ✅ **PR 1 (`ISS-02` — Done):** Detailed Functional Requirements (`FR-01`..`FR-10`), Non-Functional Requirements (`NFR-01`..`NFR-05`), user stories with acceptance criteria, and the UML **Use Case Diagram** in `docs/ba/REQUIREMENTS_AND_USE_CASES.md`.
  - ✅ **PR 2 (`ISS-15` — Done):** Created UML **Activity Diagrams** (with swimlanes) for:
    1. Offline QR Generation -> POS Local Verification -> Deferred Batch Sync
    2. GPS Telemetry -> H3 Bottleneck Detection -> Geofenced Flash Discount
  - **PR 3 (`ISS-26` — Open):** Complete the Requirements Traceability Matrix mapping every `FR-xx` to its Java/React file and QA test case.

---

### Role F: QA / Tester
- **What the Architect already provided:**
  - `.github/workflows/ci.yml` automated pipeline
  - `docs/qa/TEST_PLAN.md` template and skeletal test class `backend/src/test/java/com/pulsevenue/PulseVenueTest.java`
- **PR Implementation Plan:**
  - **PR 1 (`ISS-08`):** Define formal test cases (`TC-01`..`TC-10`) in `docs/qa/TEST_PLAN.md` and write unit tests for valid QR generation and offline verification.
  - **PR 2 (`ISS-17`):** Implement negative/security unit & integration tests in `PulseVenueTest.java`: tampered QR balance (signature mismatch), expired QR token, insufficient offline balance, and duplicate `idempotencyKey` batch replay.
  - **PR 3 (`ISS-25`):** Execute the 80,000-agent stress test benchmark, assert `<2000ms` tick execution time, and document the QA test report.

---

### Role G: Technical Writer
- **What the Architect already provided:**
  - Root `README.md` overview and `docs/technical_writing/USER_AND_API_GUIDE.md` skeleton.
- **PR Implementation Plan:**
  - **PR 1 (`ISS-09`):** Document developer environment setup (Java 21, Maven, Node/React, Docker PostgreSQL) and `curl` examples for every `/api/v1/*` endpoint in `docs/technical_writing/USER_AND_API_GUIDE.md`.
  - **PR 2 (`ISS-23`):** Write step-by-step User Manuals for the 3 personas (Attendee, Bartender POS, Event Organizer).
  - **PR 3 (`ISS-27`):** Write the final 5-minute live demo script and release notes for the project defense.
