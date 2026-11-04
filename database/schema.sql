-- PulseVenue PostgreSQL Database Schema Skeleton
-- Defined by: Software Architect (ISS-03)
-- To be completed via Pull Requests by: Database Engineer (ISS-04, ISS-21)

CREATE TABLE IF NOT EXISTS wallets (
    wallet_id VARCHAR(64) PRIMARY KEY,
    owner_name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL CHECK (role IN ('ATTENDEE', 'BARTENDER_POS', 'ORGANIZER')),
    balance NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0.00),
    currency VARCHAR(8) NOT NULL DEFAULT 'RON',
    updated_at BIGINT NOT NULL
);

-- TODO (ISS-04 - Database Engineer):
-- Complete ledger_entries, zones, and flash_discounts DDL with foreign keys and CHECK constraints.
CREATE TABLE IF NOT EXISTS ledger_entries (
    entry_id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    debit_wallet_id VARCHAR(64) NOT NULL REFERENCES wallets(wallet_id),
    credit_wallet_id VARCHAR(64) NOT NULL REFERENCES wallets(wallet_id),
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0.00),
    pos_terminal_id VARCHAR(64) NOT NULL,
    qr_nonce VARCHAR(64) NOT NULL,
    created_offline_at BIGINT NOT NULL,
    synced_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS zones (
    zone_id VARCHAR(64) PRIMARY KEY,
    h3_index VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    zone_type VARCHAR(32) NOT NULL CHECK (zone_type IN ('STAGE', 'BAR', 'FOOD', 'GATE')),
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    current_count INTEGER NOT NULL DEFAULT 0 CHECK (current_count >= 0),
    bottleneck_threshold NUMERIC(4, 2) NOT NULL DEFAULT 0.85,
    status VARCHAR(32) NOT NULL DEFAULT 'NORMAL' CHECK (status IN ('NORMAL', 'ELEVATED', 'BOTTLENECK')),
    updated_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS flash_discounts (
    discount_id VARCHAR(128) PRIMARY KEY,
    congested_zone_id VARCHAR(64) NOT NULL REFERENCES zones(zone_id),
    target_zone_id VARCHAR(64) NOT NULL REFERENCES zones(zone_id),
    discount_percent INTEGER NOT NULL CHECK (discount_percent BETWEEN 5 AND 90),
    promo_message TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL
);

-- TODO (ISS-21 - Database Engineer):
-- Add composite performance indexes and v_ledger_reconciliation view.
