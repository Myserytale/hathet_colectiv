# Technical Documentation — Setup, API Guide & User Manual (Skeleton)

**Assigned Role:** Technical Writer  
**Related Issues:** `ISS-09` (Sprint 1), `ISS-23` (Sprint 3), `ISS-27` (Sprint 4)

---

## 1. Local Developer Setup (`TODO: ISS-09`)
> **Task for Technical Writer (`ISS-09` Pull Request):**
> Document prerequisites (Java 21, Maven, Node.js 20+, Docker) and step-by-step commands to launch PostgreSQL, the Java backend, and the React Vite frontend.

---

## 2. REST API Endpoint Reference (`TODO: ISS-09`)
> **Task for Technical Writer (`ISS-09` Pull Request):**
> Document request/response schemas and sample `curl` commands for every endpoint defined in `docs/architecture/ARCHITECTURE.md`:
> - `GET /api/v1/health`
> - `GET /api/v1/state`
> - `POST /api/v1/qr/generate`
> - `POST /api/v1/qr/verify-offline`
> - `POST /api/v1/transactions/sync`
> - `POST /api/v1/telemetry/simulate-tick`

---

## 3. End-User Operational Manual (`TODO: ISS-23`)
> **Task for Technical Writer (`ISS-23` Pull Request):**
> Write user walkthroughs for:
> 1. **Attendee:** Generating a rotating QR code & claiming a Flash Discount.
> 2. **Bartender:** Operating the POS tablet in Offline Mode & syncing batches.
> 3. **Organizer:** Monitoring H3 crowd bottlenecks & running the 80k simulator.

---

## 4. Final Defense Demo Script (`TODO: ISS-27`)
> **Task for Technical Writer (`ISS-27` Pull Request):** Write the 5-minute step-by-step live presentation script.
