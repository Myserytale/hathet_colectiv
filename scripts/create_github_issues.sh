#!/usr/bin/env bash
# Script for Project Manager / GitHub Actions to create all role issues on GitHub idempotently
set -euo pipefail

if ! command -v gh &>/dev/null; then
  echo "Error: GitHub CLI ('gh') is not installed."
  exit 1
fi

existing_titles="$(gh issue list --limit 100 --state all --json title --jq '.[].title' || true)"

create_issue() {
  local code="$1"
  local title="$2"
  local body="$3"
  if echo "$existing_titles" | grep -Fq "[$code]"; then
    echo "Skipping [$code] (already exists)"
    return 0
  fi
  echo "Creating GitHub Issue: $title"
  gh issue create --title "$title" --body "$body"
}

create_issue "ISS-02" "[ISS-02] [Business Analyst] Define Functional/Non-Functional Requirements & Use Case Diagram" \
"### Role: Business Analyst (Sprint 1 - 3 SP)
- **Target File:** \`docs/ba/REQUIREMENTS_AND_USE_CASES.md\`
- **Task:** Complete \`FR-01\`..\`FR-10\`, \`NFR-01\`..\`NFR-05\`, actor descriptions, and the Mermaid UML Use Case diagram."

create_issue "ISS-04" "[ISS-04] [Database] Complete PostgreSQL Schema, Seed Data & ER Diagram" \
"### Role: Database Engineer (Sprint 1 - 3 SP)
- **Target Files:** \`database/schema.sql\`, \`database/seed.sql\`, \`database/ER_DIAGRAM.md\`
- **Task:** Finalize DDL constraints for \`wallets\`, \`ledger_entries\` (\`UNIQUE(idempotency_key)\`), \`zones\`, and \`flash_discounts\`, add initial festival seed data, and complete the Mermaid ER diagram."

create_issue "ISS-05" "[ISS-05] [Backend] Implement HMAC-SHA256 Rotating QR Signer & Offline Verifier" \
"### Role: Backend Developer 1 (Sprint 1 - 5 SP)
- **Target File:** \`backend/src/main/java/com/pulsevenue/service/PulseVenueService.java\`
- **Task:** Replace \`TODO (ISS-05)\` stubs in \`generateQrToken()\` and \`verifyQrTokenOffline()\` using Java \`javax.crypto.Mac\` (\`HmacSHA256\`) and constant-time comparison."

create_issue "ISS-06" "[ISS-06] [Frontend] Build React 3-Tab Shell & Attendee Wallet QR Component" \
"### Role: Frontend Developer (Sprint 1 - 5 SP)
- **Target Files:** \`frontend/src/App.jsx\`, \`frontend/src/components/\`
- **Task:** Replace \`TODO (ISS-06)\` in \`App.jsx\` with the \`AttendeeWallet\` React component displaying wallet balance, rotating signed QR payload, countdown timer, and active flash discount banner."

create_issue "ISS-07" "[ISS-07] [UI Designer] Create UI Wireframes, Festival Dark Theme & Status Tokens" \
"### Role: UI Designer (Sprint 1 - 3 SP)
- **Target Files:** \`docs/ui/UI_DESIGN_SPEC.md\`, \`frontend/src/styles.css\`
- **Task:** Replace \`TODO (ISS-07)\` with wireframes for Phone/Tablet/Desktop and CSS variables for Online/Offline/Bottleneck states."

create_issue "ISS-08" "[ISS-08] [QA / Tester] Write Initial Test Plan (TC-01..TC-04) & QR Crypto Unit Tests" \
"### Role: QA / Tester (Sprint 1 - 3 SP)
- **Target Files:** \`docs/qa/TEST_PLAN.md\`, \`backend/src/test/java/com/pulsevenue/PulseVenueTest.java\`
- **Task:** Complete test strategy in \`TEST_PLAN.md\` and implement automated unit tests \`TC-01\`..\`TC-04\` in \`PulseVenueTest.java\`."

create_issue "ISS-09" "[ISS-09] [Technical Writer] Write Developer Setup Guide & REST API Reference" \
"### Role: Technical Writer (Sprint 1 - 2 SP)
- **Target Files:** \`docs/technical_writing/USER_AND_API_GUIDE.md\`, \`README.md\`
- **Task:** Document Java 21 + React + Docker PostgreSQL setup and \`curl\` examples for all \`/api/v1/*\` endpoints."

create_issue "ISS-10" "[ISS-10] [Database & Backend] Implement Atomic Double-Entry Batch Sync with Idempotency" \
"### Role: Backend Developer 1 & Database Engineer (Sprint 2 - 5 SP)
- **Target Files:** \`backend/src/main/java/com/pulsevenue/service/PulseVenueService.java\`, \`database/schema.sql\`
- **Task:** Implement \`syncOfflineBatch()\` so each transaction atomically debits the attendee wallet, credits the bar POS wallet, and skips duplicate \`idempotencyKey\` submissions."

create_issue "ISS-11" "[ISS-11] [Backend] Expose REST API Endpoints for State, QR & Batch Sync" \
"### Role: Backend Developer 1 (Sprint 2 - 5 SP)
- **Target File:** \`backend/src/main/java/com/pulsevenue/PulseVenueApplication.java\`
- **Task:** Expose \`/api/v1/state\`, \`/api/v1/qr/generate\`, \`/api/v1/qr/verify-offline\`, and \`/api/v1/transactions/sync\` with CORS headers for React."

create_issue "ISS-12" "[ISS-12] [Backend] Implement H3 Zone Bottleneck Detector & Flash Discount Engine" \
"### Role: Backend Developer 2 (Sprint 2 - 5 SP)
- **Target File:** \`backend/src/main/java/com/pulsevenue/service/PulseVenueService.java\`
- **Task:** Implement \`updateZoneTelemetry()\` to flag \`BOTTLENECK\` when \`occupancyRatio >= 0.85\` and create a 20% \`FlashDiscount\` redirecting traffic to the nearest \`NORMAL\` bar."

create_issue "ISS-13" "[ISS-13] [Frontend] Implement Bartender Offline Tablet POS & localStorage Queue" \
"### Role: Frontend Developer (Sprint 2 - 5 SP)
- **Target File:** \`frontend/src/App.jsx\`
- **Task:** Build the Bartender POS tab with Online/Offline toggle, local QR signature & balance verification, \`localStorage\` offline queue, and 'Sync Batch to Cloud Ledger' button."

create_issue "ISS-14" "[ISS-14] [Frontend] Connect Organizer Dashboard to Live H3 Zone Telemetry & Promos" \
"### Role: Frontend Developer (Sprint 2 - 3 SP)
- **Target File:** \`frontend/src/App.jsx\`
- **Task:** Render H3 zone occupancy cards, bottleneck alerts, and active flash discounts in the Organizer tab."

create_issue "ISS-15" "[ISS-15] [Business Analyst] Create UML Activity Diagrams for Offline Sync & Crowd Redirection" \
"### Role: Business Analyst (Sprint 2 - 3 SP)
- **Target File:** \`docs/ba/REQUIREMENTS_AND_USE_CASES.md\`
- **Task:** Add swimlane UML Activity diagrams for Offline QR Payment -> Batch Sync and GPS Telemetry -> Bottleneck Flash Discount."

create_issue "ISS-16" "[ISS-16] [UI Designer] Style H3 Zone Heatmap Cards, POS Keypad & Flash Promo Banner" \
"### Role: UI Designer (Sprint 2 - 3 SP)
- **Target File:** \`frontend/src/styles.css\`
- **Task:** Style the H3 zone occupancy bars (Green/Amber/Red), large tablet POS buttons, and pulsing flash discount banner."

create_issue "ISS-17" "[ISS-17] [QA / Tester] Add Security & Edge-Case Tests (TC-05..TC-07)" \
"### Role: QA / Tester (Sprint 2 - 5 SP)
- **Target File:** \`backend/src/test/java/com/pulsevenue/PulseVenueTest.java\`
- **Task:** Add automated tests verifying tampered QR rejection, expired token rejection, double-entry money conservation, and idempotent batch retry."

create_issue "ISS-19" "[ISS-19] [Backend] Build 80,000-Agent Autonomous Crowd & Commerce Simulator" \
"### Role: Backend Developer 2 (Sprint 3 - 5 SP)
- **Target File:** \`backend/src/main/java/com/pulsevenue/simulator/CrowdSimulator.java\`
- **Task:** Replace \`TODO (ISS-19)\` stub in \`runTick()\` to model 80,000 attendees moving across H3 zones, triggering bottlenecks and syncing sample payment batches."

create_issue "ISS-21" "[ISS-21] [Database] Add Composite Indexes & Double-Entry Reconciliation SQL View" \
"### Role: Database Engineer (Sprint 3 - 3 SP)
- **Target File:** \`database/schema.sql\`
- **Task:** Add composite indexes and a \`v_ledger_reconciliation\` SQL view verifying \`SUM(debit) == SUM(credit)\`."

create_issue "ISS-22" "[ISS-22] [Frontend] Add Visual QR Matrix SVG Renderer & Simulator Control Panel" \
"### Role: Frontend Developer (Sprint 3 - 3 SP)
- **Target File:** \`frontend/src/App.jsx\`
- **Task:** Add visual QR matrix rendering on the Attendee tab and a 'Run 80,000-Agent Wave' button on the Organizer tab."

create_issue "ISS-23" "[ISS-23] [Technical Writer] Write Role-by-Role User Manuals (Attendee, Bartender, Organizer)" \
"### Role: Technical Writer (Sprint 3 - 3 SP)
- **Target File:** \`docs/technical_writing/USER_AND_API_GUIDE.md\`
- **Task:** Write operational guides for Attendees, Bartenders, and Event Organizers."

create_issue "ISS-25" "[ISS-25] [QA / Tester] Execute 80,000-Agent Stress Benchmark (TC-08) & Publish QA Report" \
"### Role: QA / Tester (Sprint 4 - 5 SP)
- **Target Files:** \`docs/qa/TEST_PLAN.md\`, \`backend/src/test/java/com/pulsevenue/PulseVenueTest.java\`
- **Task:** Implement \`TC-08\` asserting that an 80,000-agent simulation tick completes in \`< 2000 ms\` and document benchmark results."

echo "✅ All GitHub Role Issues synced!"
