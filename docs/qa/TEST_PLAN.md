# QA Test Plan & Verification Matrix

**Assigned Role:** QA / Tester  
**Related Issues:** `ISS-08` (Sprint 1), `ISS-17` (Sprint 2), `ISS-25` (Sprint 4)  
**Test Entry Point:** [`backend/src/test/java/com/pulsevenue/PulseVenueTest.java`](../../backend/src/test/java/com/pulsevenue/PulseVenueTest.java)  
**Simulator Runner:** [`backend/src/main/java/com/pulsevenue/simulator/SimulatorTest.java`](../../backend/src/main/java/com/pulsevenue/simulator/SimulatorTest.java)

---

## 1. Test Strategy (`TODO: ISS-08`)
- **Currently Passing in CI (`bash scripts/verify_skeleton.sh`):**
  - Java 21 compilation + `PulseVenueTest.testSkeletonContractCompilesAndRuns()` (verifies `generateQrToken` contract).
  - React production bundle (`npm run build`).
  - PostgreSQL 16 live execution of `database/schema.sql` and `database/seed.sql`.
- **Docker E2E Demo Smoke Check (`bash scripts/demo.sh smoke`):**
  - Verifies Nginx reverse proxy (`/api/v1/*`), signed QR generation, offline verification, idempotent batch sync retry, H3 bottleneck flash discount trigger, and 80,000-agent simulator ticks inside Docker.
- **Task for QA (`ISS-08` Pull Request):** Document formal unit, integration, offline security, and 80,000-agent load test criteria below.

---

## 2. Test Case Catalog (`TC-01`..`TC-08`)

| Test ID | Target Issue | Scenario Description | Expected Result | Status |
| :--- | :---: | :--- | :--- | :---: |
| **TC-01** | `ISS-08` | Valid rotating QR token generation & offline verification | `valid == true`, correct remaining balance | 📋 Todo |
| **TC-02** | `ISS-17` | Tampered `balanceSnapshot` in QR payload | Rejected (`Invalid HMAC signature`) | 📋 Todo |
| **TC-03** | `ISS-17` | Expired QR token (`checkTimestamp > expiresAt`) | Rejected (`Token expired`) | 📋 Todo |
| **TC-04** | `ISS-08` | Order exceeds remaining offline balance | Rejected (`Insufficient balance`) | 📋 Todo |
| **TC-05** | `ISS-17` | Double-entry batch sync balance conservation | `SUM(wallets.balance)` remains invariant | 📋 Todo |
| **TC-06** | `ISS-17` | Duplicate offline batch submission (`idempotencyKey` retry) | Second upload skipped (`skippedDuplicates == 1`) | 📋 Todo |
| **TC-07** | `ISS-17` | Zone occupancy `>= 85%` triggers Flash Discount | Zone flagged congested, 20% promo created for `NORMAL` relief bar | 📋 Todo |
| **TC-08** | `ISS-25` | 80,000-Agent Simulator tick performance benchmark | Completes in `< 2000 ms` with 0 ledger discrepancies | 📋 Todo |
