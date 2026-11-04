# QA Test Plan & Verification Matrix (Skeleton)

**Assigned Role:** QA / Tester  
**Related Issues:** `ISS-08` (Sprint 1), `ISS-17` (Sprint 2), `ISS-25` (Sprint 4)  
**Test Class:** `backend/src/test/java/com/pulsevenue/PulseVenueTest.java`

---

## 1. Test Strategy (`TODO: ISS-08`)
> **Task for QA (`ISS-08` Pull Request):** Document unit, integration, offline security, and 80,000-agent load test criteria.

---

## 2. Test Case Catalog (`TC-01`..`TC-08`)

| Test ID | Target Issue | Scenario Description | Expected Result (`TODO: QA`) | Status |
| :--- | :---: | :--- | :--- | :---: |
| **TC-01** | `ISS-08` | Valid rotating QR token generation & offline verification | `valid == true`, correct remaining balance | 📋 Todo |
| **TC-02** | `ISS-17` | Tampered `balanceSnapshot` in QR payload | Rejected (`Invalid cryptographic signature`) | 📋 Todo |
| **TC-03** | `ISS-17` | Expired QR token (`checkTimestamp > expiresAt`) | Rejected (`QR token has expired`) | 📋 Todo |
| **TC-04** | `ISS-08` | Order exceeds remaining offline balance | Rejected (`Insufficient offline balance`) | 📋 Todo |
| **TC-05** | `ISS-17` | Double-entry batch sync balance conservation | `SUM(wallets.balance)` remains invariant | 📋 Todo |
| **TC-06** | `ISS-17` | Duplicate offline batch submission (`idempotencyKey` retry) | Second upload skipped (`skippedDuplicates == 1`) | 📋 Todo |
| **TC-07** | `ISS-17` | Zone occupancy `>= 85%` triggers Flash Discount | Zone marked `BOTTLENECK`, promo created for relief bar | 📋 Todo |
| **TC-08** | `ISS-25` | 80,000-Agent Simulator tick performance benchmark | Completes in `< 2000 ms` | 📋 Todo |
