#!/usr/bin/env bash
set -euo pipefail

echo "=== 1. Compiling Java 21 Backend Skeleton & Running QA Smoke Test ==="
mkdir -p backend/target/classes
javac -d backend/target/classes $(find backend/src -name "*.java")
java -cp backend/target/classes com.pulsevenue.PulseVenueTest

echo "=== 2. Checking React Frontend Skeleton Files ==="
test -f frontend/package.json
test -f frontend/vite.config.js
test -f frontend/src/App.jsx
test -f frontend/src/styles.css

echo "=== 3. Checking PostgreSQL Schema & Role Documentation Files ==="
test -s database/schema.sql
test -s database/seed.sql
test -s docs/architecture/ARCHITECTURE.md
test -s docs/pm/SIMPLIFICATION_ROADMAP.md
test -s docs/pm/TEAM_ROLES_AND_PLANS.md
test -s docs/pm/ISSUES_BACKLOG.md

echo "✅ All PulseVenue CI/CD Skeleton Checks Passed!"
