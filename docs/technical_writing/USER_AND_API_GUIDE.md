# Technical Documentation — Setup, API Reference & User Manual

**Assigned Role:** Technical Writer  
**Related Issues:** `ISS-09` (Sprint 1), `ISS-23` (Sprint 3), `ISS-27` (Sprint 4)

---

## 1. Quick Start: Docker & Local Development (`ISS-09`)

### Option A: Full Stack in Docker (Recommended for Demos)
Prerequisites: **Docker** and **Docker Compose**.

```bash
bash scripts/demo.sh          # Build & start PostgreSQL 16 + Java 21 Backend + React Nginx (:3000)
bash scripts/demo.sh smoke    # Run live E2E API, idempotency, 80k simulator & DB check
bash scripts/demo.sh sim      # Run 80,000-agent simulator ticks inside Docker
bash scripts/demo.sh logs     # Stream container logs
bash scripts/demo.sh down     # Stop containers and reset volumes
```

- **Frontend (Nginx + `/api/` proxy):** `http://localhost:3000`
- **Backend API Direct:** `http://localhost:8080`
- **PostgreSQL 16:** `localhost:5432` (`db=pulsevenue`, `user=pulsevenue`, `password=pulsevenue_dev_pass`)

### Option B: Run Without Docker
Prerequisites: **Java 21 (JDK)**, **Node.js 22+**, and **PostgreSQL 16** (optional for in-memory backend mode).

```bash
# 1. Verify compilation & run smoke test
bash scripts/verify_skeleton.sh

# 2. Start Backend on :8080
javac -d backend/target/classes $(find backend/src/main/java -name "*.java")
java -cp backend/target/classes com.pulsevenue.PulseVenueApplication

# 3. Start Frontend dev server on :5173 (in a second terminal)
cd frontend && npm install && npm run dev
```

---

## 2. REST API Endpoint Reference (`ISS-09`)

Base URL: `http://localhost:3000` (via Nginx proxy) or `http://localhost:8080` (direct).

### `GET /api/v1/health`
```bash
curl -s http://localhost:3000/api/v1/health
```

### `GET /api/v1/state`
```bash
curl -s http://localhost:3000/api/v1/state
```

### `POST /api/v1/qr/generate`
```bash
curl -s -X POST http://localhost:3000/api/v1/qr/generate \
  -H "Content-Type: application/json" \
  -d '{"walletId":"w_attendee_01","ttlSeconds":300}'
```

### `POST /api/v1/qr/verify-offline`
```bash
curl -s -X POST http://localhost:3000/api/v1/qr/verify-offline \
  -H "Content-Type: application/json" \
  -d '{"walletId":"w_attendee_01","ownerName":"Alex Popescu (Attendee)","balanceSnapshot":180.00,"currency":"RON","nonce":"<NONCE>","issuedAt":<ISSUED_AT>,"expiresAt":<EXPIRES_AT>,"signature":"<SIGNATURE>","orderAmount":25.00,"localAlreadySpent":0.00}'
```

### `POST /api/v1/transactions/sync`
```bash
curl -s -X POST http://localhost:3000/api/v1/transactions/sync \
  -H "Content-Type: application/json" \
  -d '{"posTerminalId":"POS_BAR_NORTH_01","transactions":[{"idempotencyKey":"tx_demo_001","debitWalletId":"w_attendee_01","creditWalletId":"w_bar_north","amount":25.00,"createdOfflineAt":1762250015}]}'
```

### `POST /api/v1/telemetry/simulate-tick`
```bash
curl -s -X POST http://localhost:3000/api/v1/telemetry/simulate-tick \
  -H "Content-Type: application/json" \
  -d '{"8a2a1072b51ffff":14000}'
```

---

## 3. End-User Operational Manual (`TODO: ISS-23`)
> **Task for Technical Writer (`ISS-23` Pull Request):**
> Once the React UI tabs (`ISS-06`, `ISS-13`, `ISS-14`, `ISS-22`) are merged, write step-by-step guides with screenshots for:
> 1. **Attendee:** Generating a rotating QR code & claiming a Flash Discount.
> 2. **Bartender:** Operating the POS tablet in Offline Mode & syncing batches.
> 3. **Organizer:** Monitoring H3 crowd bottlenecks & running the 80k simulator.

---

## 4. Final Defense Demo Script (`TODO: ISS-27`)
> **Task for Technical Writer (`ISS-27` Pull Request):** Finalize the 5-minute step-by-step live presentation script and `v1.0.0` release notes.
