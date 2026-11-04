package com.pulsevenue.service;

import com.pulsevenue.model.Models.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Architectural Service Contract for PulseVenue.
 * Defined by: Software Architect (ISS-03).
 * To be implemented via Pull Requests by: Backend Developers (ISS-05, ISS-10, ISS-12).
 */
public class PulseVenueService {

    public static final String VENUE_HMAC_SECRET = "pulsevenue-offline-signing-secret-2026";

    /**
     * TODO (ISS-05 - Backend Developer 1):
     * Implement HMAC-SHA256 rotating QR token generation.
     * Canonical payload format (ADR-001):
     * "walletId|balanceSnapshot(2 decimals)|currency|nonce|issuedAt|expiresAt"
     */
    public QrToken generateQrToken(String walletId, int ttlSeconds, long nowEpochSeconds) {
        // Skeletal stub for Sprint 1 compilation — replace in PR for ISS-05
        return new QrToken(
                walletId,
                "Stub Attendee",
                180.00,
                "RON",
                "stub_nonce_01",
                nowEpochSeconds,
                nowEpochSeconds + ttlSeconds,
                "TODO_IMPLEMENT_HMAC_SHA256_IN_ISS_05"
        );
    }

    /**
     * TODO (ISS-05 - Backend Developer 1):
     * Implement 100% offline QR signature verification, expiration check, and balance check.
     */
    public OfflineVerificationResult verifyQrTokenOffline(
            QrToken token, double orderAmount, double localAlreadySpent, long checkTimestamp) {
        // Skeletal stub for Sprint 1 compilation — replace in PR for ISS-05
        if (token == null || orderAmount <= 0) {
            return new OfflineVerificationResult(false, "Invalid parameters", 0.0);
        }
        return new OfflineVerificationResult(
                true,
                "Stub verification (TODO: implement HMAC & expiration check in ISS-05)",
                Math.max(0.0, token.balanceSnapshot() - localAlreadySpent - orderAmount)
        );
    }

    /**
     * TODO (ISS-10 - Backend Developer 1 & Database Engineer):
     * Implement atomic double-entry ledger batch synchronization with idempotencykey deduplication.
     */
    public SyncBatchResult syncOfflineBatch(
            String posTerminalId, List<OfflineTransaction> transactions, long syncTimestamp) {
        // Skeletal stub for Sprint 1 compilation — replace in PR for ISS-10
        int count = transactions == null ? 0 : transactions.size();
        return new SyncBatchResult(posTerminalId, count, 0, List.of());
    }

    /**
     * TODO (ISS-12 - Backend Developer 2):
     * Update Uber H3 zone attendee headcounts, detect crowd bottlenecks (occupancy >= 0.85),
     * and trigger a 20% FlashDiscount at the nearest NORMAL bar/food zone.
     */
    public List<FlashDiscount> updateZoneTelemetry(Map<String, Integer> newZoneCounts, long nowTimestamp) {
        // Skeletal stub for Sprint 1 compilation — replace in PR for ISS-12
        return List.of();
    }

    public List<Wallet> getWallets() {
        long now = Instant.now().getEpochSecond();
        return List.of(
                new Wallet("w_attendee_01", "Alex Popescu (Attendee)", "ATTENDEE", 180.00, "RON", now),
                new Wallet("w_bar_north", "North Arena Bar POS", "BARTENDER_POS", 5000.00, "RON", now)
        );
    }

    public List<H3Zone> getZones() {
        long now = Instant.now().getEpochSecond();
        return List.of(
                new H3Zone("Z_STAGE_MAIN", "8a2a1072b59ffff", "Main Stage Arena", "STAGE", 45000, 28500, 0.85, "ELEVATED", 0.633, now),
                new H3Zone("Z_BAR_NORTH", "8a2a1072b51ffff", "North Main Bar", "BAR", 15000, 11800, 0.85, "ELEVATED", 0.787, now),
                new H3Zone("Z_BAR_SOUTH", "8a2a1072b12ffff", "South Chill Bar", "BAR", 18000, 4400, 0.85, "NORMAL", 0.244, now)
        );
    }
}
