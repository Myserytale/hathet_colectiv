-- PulseVenue Initial Seed Data Skeleton (PostgreSQL)
-- To be expanded via Pull Request by: Database Engineer (ISS-04)

INSERT INTO wallets (wallet_id, owner_name, role, balance, currency, updated_at) VALUES
('w_attendee_01', 'Alex Popescu (Attendee)', 'ATTENDEE', 180.00, 'RON', 1762250000),
('w_bar_north', 'North Arena Bar POS', 'BARTENDER_POS', 5000.00, 'RON', 1762250000)
ON CONFLICT (wallet_id) DO NOTHING;

-- TODO (ISS-04 - Database Engineer): Add remaining attendee/bar wallets and initial H3 zones.
