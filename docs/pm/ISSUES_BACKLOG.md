# PulseVenue — GitHub Issues Backlog & PR Plan (Java 21 + React + PostgreSQL)

> **Workflow:** Pick an open issue below, create a branch `feat/ISS-XX-short-name`, implement the scoped deliverable, and open a Pull Request against `main`.

---

## Sprint 1: Barebone Foundation & Contracts (Weeks 1–2)

| ID | Role | Status | Title & Target Files | SP | PR Acceptance Criteria |
| :--- | :--- | :---: | :--- | :---: | :--- |
| **ISS-01** | **Project Manager / DevOps** | ✅ **DONE** | Initialize Repo, GitHub Actions CI/CD, Docker Compose & PR/Issue Templates (`.github/`, `docker-compose.yml`, `docs/pm/`) | 3 | CI workflow runs Java 21 + React + SQL validation; PR & Issue templates active. |
| **ISS-02** | **Business Analyst** | ✅ **DONE** | Define Functional/Non-Functional Requirements & UML Use Case Diagram (`docs/ba/REQUIREMENTS_AND_USE_CASES.md`) | 3 | Document `FR-01`..`FR-10`, `NFR-01`..`NFR-05`, actor descriptions, and Mermaid Use Case diagram. |
| **ISS-03** | **Software Architect** | ✅ **DONE** | Create C4 Architecture Diagrams, Sequence Diagrams & Freeze Java/React REST Contracts (`docs/architecture/ARCHITECTURE.md`, `Models.java`) | 5 | C4 Context, Container, Sequence diagrams + frozen JSON DTOs in `Models.java` committed. |
| **ISS-04** | **Database Engineer** | 📋 Open | Complete PostgreSQL Schema, Seed Data & ER Diagram (`database/schema.sql`, `database/seed.sql`, `database/ER_DIAGRAM.md`) | 3 | Tables `wallets`, `ledger_entries`, `zones`, `flash_discounts` with PK/FK/UNIQUE constraints & Mermaid ER diagram. |
| **ISS-05** | **Backend Developer 1** | ✅ **DONE** | Implement HMAC-SHA256 Rotating QR Signer & Offline Verifier (`PulseVenueService.java`) | 5 | Implement `generateQrToken()` and `verifyQrTokenOffline()` using `HmacSHA256` and constant-time comparison. |
| **ISS-06** | **Frontend Developer** | ✅ **DONE** | Build React 3-Tab Shell & Attendee Wallet QR Component (`frontend/src/App.jsx`, `frontend/src/components/AttendeeWallet.jsx`) | 5 | Attendee tab renders wallet selector, balance, rotating QR token payload, and countdown timer. |
| **ISS-07** | **UI Designer** | ✅ **DONE** | Create UI Wireframes, Festival Dark Theme & Status Tokens (`docs/ui/ISS-07_DESIGN_FOUNDATIONS.md`, `frontend/src/styles.css`) | 3 | Complete wireframes for Phone/Tablet/Desktop and CSS variables for Online/Offline/Bottleneck states. |
| **ISS-08** | **QA / Tester** | ✅ **DONE** | Write Initial Test Plan (`TC-01`..`TC-04`) & QR Crypto Unit Tests (`docs/qa/TEST_PLAN.md`, `QrCryptoUnitTests.java`) | 3 | Document test strategy and implement unit tests for valid QR generation and offline verification. |
| **ISS-09** | **Technical Writer** | 📋 Open | Write Developer Setup Guide & REST API Reference (`docs/technical_writing/USER_AND_API_GUIDE.md`, `README.md`) | 2 | Document Java 21 + React + Docker PostgreSQL setup and `curl` examples for all endpoints. |

---

## Sprint 2: Working End-to-End MVP (Weeks 3–5)

| ID | Role | Status | Title & Target Files | SP | PR Acceptance Criteria |
| :--- | :--- | :---: | :--- | :---: | :--- |
| **ISS-10** | **Database & Backend 1** | ✅ **DONE** | Implement Atomic Double-Entry Batch Sync with Idempotency (`PulseVenueService.java`, `database/schema.sql`) | 5 | `syncOfflineBatch()` debits attendee, credits bar POS, and skips duplicate `idempotencyKey`s. |
| **ISS-11** | **Backend Developer 1** | ✅ **DONE** | Expose REST API Endpoints for State, QR & Batch Sync (`PulseVenueApplication.java`) | 5 | Serve `/api/v1/state`, `/api/v1/qr/generate`, `/api/v1/qr/verify-offline`, `/api/v1/transactions/sync` with CORS. |
| **ISS-12** | **Backend Developer 2** | ✅ **DONE** | Implement H3 Zone Bottleneck Detector & Flash Discount Engine (`PulseVenueService.java`) | 5 | When zone `currentCount / capacity >= 0.85`, status becomes `BOTTLENECK` and triggers a 20% discount at a `NORMAL` bar. |
| **ISS-13** | **Frontend Developer** | ✅ **DONE** | Implement Bartender Offline Tablet POS & `localStorage` Queue (`frontend/src/components/BartenderPos.jsx`) | 5 | Bartender can toggle Offline Mode, verify QR locally, queue txs in `localStorage`, and click "Sync Batch to Ledger". |
| **ISS-14** | **Frontend Developer** | ✅ **DONE** | Connect Organizer Dashboard to Live H3 Zone Telemetry & Promos (`frontend/src/components/OrganizerDashboard.jsx`) | 3 | Organizer tab displays occupancy bars, `BOTTLENECK` badges, and active flash discount banners. |
| **ISS-15** | **Business Analyst** | ✅ **DONE** | Create UML Activity Diagrams for Offline Sync & Crowd Redirection (`docs/ba/REQUIREMENTS_AND_USE_CASES.md`) | 3 | Add swimlane activity diagrams covering both happy paths and error/rejection branches. |
| **ISS-16** | **UI Designer** | 📋 Open | Style H3 Zone Heatmap Cards, POS Keypad & Flash Promo Banner (`frontend/src/styles.css`) | 3 | Add responsive grid cards, progress bars (Green/Amber/Red), and pulsing discount banner. |
| **ISS-17** | **QA / Tester** | ✅ **DONE** | Add Security & Edge-Case Tests (`TC-05`..`TC-07`: Tampered QR, Expired Token, Duplicate Sync) (`SecurityAndSyncTests.java`) | 5 | Automated tests verify tampered balance rejection, expired token rejection, and idempotent batch retry. |
| **ISS-18** | **Project Manager** | 📋 Open | Conduct Sprint 2 End-to-End MVP Integration Review | 2 | All PRs merged to `main`, CI green, working demo of offline payment -> batch sync -> bottleneck promo. |

---

## Sprint 3: 80k Simulator, Visual Polish & DB Indexing (Weeks 6–8)

| ID | Role | Status | Title & Target Files | SP | PR Acceptance Criteria |
| :--- | :--- | :---: | :--- | :---: | :--- |
| **ISS-19** | **Backend Developer 2** | ✅ **DONE** | Build 80,000-Agent Autonomous Crowd & Commerce Simulator (`CrowdSimulator.java`) | 5 | Simulate 80,000 attendees moving across H3 zones, triggering bottlenecks and settling sample tx batches. |
| **ISS-20** | **Software Architect** | 📋 Open | Document Scalability Model & Virtual Thread Concurrency ADR (`docs/architecture/ARCHITECTURE.md`) | 3 | Add ADR-004 detailing vectorized vs Java 21 virtual-thread agent simulation. |
| **ISS-21** | **Database Engineer** | 📋 Open | Add Composite Indexes & Double-Entry Reconciliation SQL View (`database/schema.sql`) | 3 | Add `v_ledger_reconciliation` view verifying `SUM(debit) == SUM(credit)` and benchmark query plans. |
| **ISS-22** | **Frontend Developer** | 📋 Open | Add Visual QR Matrix SVG Renderer & Simulator Control Panel (`frontend/src/App.jsx`) | 3 | Render scannable QR SVG on Attendee tab and "Run 80k Agent Wave" button on Organizer tab. |
| **ISS-23** | **Technical Writer** | 📋 Open | Write Role-by-Role User Manuals (Attendee, Bartender, Organizer) (`docs/technical_writing/USER_AND_API_GUIDE.md`) | 3 | Step-by-step operational guide with screenshots and troubleshooting section. |

---

## Sprint 4: Heavy Extensions & Final Grading Deliverables (Weeks 9–12)

| ID | Role | Status | Title & Target Files | SP | PR Acceptance Criteria |
| :--- | :--- | :---: | :--- | :---: | :--- |
| **ISS-24** | **Backend Developer 2** | 📋 Open | Integrate Real Uber H3 (`com.uber:h3`) Coordinate Indexing (`pom.xml`, `PulseVenueService.java`) | 5 | Convert raw `(lat, lng)` GPS points to Resolution-10 H3 hex indices. |
| **ISS-25** | **QA / Tester** | ✅ **DONE** | Execute 80,000-Agent Stress Benchmark (`TC-08`) & Publish QA Report (`docs/qa/TEST_PLAN.md`, `SimulatorBenchmarkTests.java`) | 5 | Verify 80,000-agent tick finishes in `<2000ms` with 0 ledger discrepancies; record latency table. |
| **ISS-26** | **UI Designer & BA** | 📋 Open | Complete Requirements Traceability Matrix & UX Accessibility Audit (`docs/ba/`, `docs/ui/`) | 3 | Map every `FR-01`..`FR-10` to code class + test method; verify contrast ratios. |
| **ISS-27** | **Project Manager & Tech Writer** | 📋 Open | Final Release `v1.0.0`, Presentation Deck & Live Demo Script (`README.md`, `docs/technical_writing/`) | 3 | Tag `v1.0.0`, all CI checks green, 5-minute defense walkthrough ready. |
