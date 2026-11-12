#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

BASE_URL="${PULSEVENUE_URL:-http://localhost:3000}"

require_docker() {
  if ! docker info >/dev/null 2>&1; then
    echo "Error: Docker daemon is not running. Start Docker Desktop / dockerd first." >&2
    exit 1
  fi
}

extract_json_string() {
  local json="$1"
  local key="$2"
  printf '%s' "$json" | sed -n "s/.*\"${key}\":\"\([^\"]*\)\".*/\1/p"
}

extract_json_number() {
  local json="$1"
  local key="$2"
  printf '%s' "$json" | sed -n "s/.*\"${key}\":\([0-9.]*\).*/\1/p"
}

cmd_up() {
  require_docker
  echo "=== Building & Starting PulseVenue Demo Stack (Postgres + Java 21 + React Nginx) ==="
  docker compose up -d --build --wait

  echo ""
  echo "=== Container Status ==="
  docker compose ps

  echo ""
  echo "=== Smoke Check via Nginx Proxy ($BASE_URL/api/v1/health) ==="
  curl -fsS "$BASE_URL/api/v1/health"
  echo ""

  echo ""
  echo "=================================================================="
  echo "  PulseVenue Docker Demo Stack is READY"
  echo "=================================================================="
  echo "  Frontend (Nginx + API Proxy):  $BASE_URL"
  echo "  Backend API Direct:            http://localhost:8080/api/v1/health"
  echo "  PostgreSQL 16:                 localhost:5432 (db/user: pulsevenue)"
  echo ""
  echo "  Useful presentation commands:"
  echo "    bash scripts/demo.sh smoke   # Run full live API + DB + 80k sim walkthrough"
  echo "    bash scripts/demo.sh sim     # Run 80,000-agent simulator inside Docker"
  echo "    bash scripts/demo.sh logs    # Follow live container logs"
  echo "    bash scripts/demo.sh down    # Stop containers & reset demo data"
  echo "=================================================================="
}

cmd_sim() {
  require_docker
  echo "=== Running 80,000-Agent Simulator inside pulsevenue-backend ==="
  docker compose exec -T backend java -cp /app/app.jar com.pulsevenue.simulator.SimulatorTest
}

cmd_smoke() {
  require_docker
  echo "=== 1. Health & Initial State ($BASE_URL) ==="
  curl -fsS "$BASE_URL/api/v1/health" && echo ""
  curl -fsS "$BASE_URL/api/v1/state" && echo ""

  echo ""
  echo "=== 2. Generate Signed Rotating QR Token (POST /api/v1/qr/generate) ==="
  QR_JSON="$(curl -fsS -X POST "$BASE_URL/api/v1/qr/generate" \
    -H "Content-Type: application/json" \
    -d '{"walletId":"w_attendee_01","ttlSeconds":300}')"
  echo "$QR_JSON"

  local nonce issued_at expires_at balance_snapshot signature
  nonce="$(extract_json_string "$QR_JSON" "nonce")"
  issued_at="$(extract_json_number "$QR_JSON" "issuedAt")"
  expires_at="$(extract_json_number "$QR_JSON" "expiresAt")"
  balance_snapshot="$(extract_json_number "$QR_JSON" "balanceSnapshot")"
  signature="$(extract_json_string "$QR_JSON" "signature")"

  echo ""
  echo "=== 3. Verify QR Offline at Bartender POS (POST /api/v1/qr/verify-offline) ==="
  curl -fsS -X POST "$BASE_URL/api/v1/qr/verify-offline" \
    -H "Content-Type: application/json" \
    -d "{\"walletId\":\"w_attendee_01\",\"ownerName\":\"Alex Popescu\",\"balanceSnapshot\":${balance_snapshot},\"currency\":\"RON\",\"nonce\":\"${nonce}\",\"issuedAt\":${issued_at},\"expiresAt\":${expires_at},\"signature\":\"${signature}\",\"orderAmount\":25.00,\"localAlreadySpent\":0.00}"
  echo ""

  echo ""
  echo "=== 4. Sync Offline Batch & Prove Idempotency (POST /api/v1/transactions/sync) ==="
  local idem_key="tx_demo_$(date +%s)_001"
  local sync_payload="{\"posTerminalId\":\"POS_BAR_NORTH_01\",\"transactions\":[{\"idempotencyKey\":\"${idem_key}\",\"debitWalletId\":\"w_attendee_01\",\"creditWalletId\":\"w_bar_north\",\"amount\":25.00,\"createdOfflineAt\":${issued_at}}]}"
  echo "First upload (expect syncedCount: 1):"
  curl -fsS -X POST "$BASE_URL/api/v1/transactions/sync" -H "Content-Type: application/json" -d "$sync_payload" && echo ""
  echo "Retry upload with same idempotencyKey (expect skippedDuplicates: 1):"
  curl -fsS -X POST "$BASE_URL/api/v1/transactions/sync" -H "Content-Type: application/json" -d "$sync_payload" && echo ""

  echo ""
  echo "=== 5. Trigger H3 Crowd Bottleneck & Flash Discount (POST /api/v1/telemetry/simulate-tick) ==="
  curl -fsS -X POST "$BASE_URL/api/v1/telemetry/simulate-tick" \
    -H "Content-Type: application/json" \
    -d '{"8a2a1072b51ffff":14000}'
  echo ""

  echo ""
  echo "=== 6. Run 80,000-Agent Simulator inside Docker ==="
  cmd_sim

  echo ""
  echo "=== 7. Verify Seeded PostgreSQL 16 Wallets ==="
  docker compose exec -T postgres psql -U pulsevenue -d pulsevenue -c "SELECT wallet_id, owner_name, balance, currency FROM wallets;"
}

cmd_logs() {
  require_docker
  docker compose logs -f
}

cmd_down() {
  require_docker
  echo "=== Stopping & Resetting PulseVenue Demo Stack ==="
  docker compose down -v
}

case "${1:-up}" in
  up)    cmd_up ;;
  sim)   cmd_sim ;;
  smoke) cmd_smoke ;;
  logs)  cmd_logs ;;
  down)  cmd_down ;;
  *)
    echo "Usage: bash scripts/demo.sh [up|smoke|sim|logs|down]" >&2
    exit 1
    ;;
esac
