package com.pulsevenue.tests;

import com.pulsevenue.model.Models.*;
import com.pulsevenue.service.PulseVenueService;

import java.util.List;
import java.util.Map;

import static com.pulsevenue.tests.TestAssert.*;

/**
 * ISS-17 (QA / Tester): Security, edge-case, idempotency, and bottleneck trigger tests.
 * Test Cases: TC-02, TC-03, TC-05, TC-06, TC-07, TC-10
 */
public final class SecurityAndSyncTests {

    private static final long BASE_TIME = 1762250000L;

    private SecurityAndSyncTests() {}

    public static void runAll() {
        System.out.println("\n--- [ISS-17] Security, Idempotency & Bottleneck Tests ---");
        runTest("TC-02", "Reject tampered QR payload (HMAC mismatch)",                SecurityAndSyncTests::testTamperedQrPayloadRejected);
        runTest("TC-03", "Reject expired QR token (checkTimestamp > expiresAt)",      SecurityAndSyncTests::testExpiredQrTokenRejected);
        runTest("TC-05", "Double-entry batch sync conserves total wallet balance",     SecurityAndSyncTests::testDoubleEntryBalanceConservation);
        runTest("TC-06", "Duplicate offline batch sync skipped via idempotencyKey",   SecurityAndSyncTests::testDuplicateBatchSyncIdempotency);
        runTest("TC-07", "Zone >= 85% occupancy triggers CONGESTED & 20% promo",     SecurityAndSyncTests::testBottleneckAndFlashDiscountTrigger);
        runTest("TC-10", "Blank idempotencyKey is rejected without touching balances", SecurityAndSyncTests::testBlankIdempotencyKeyRejected);
    }

    /**
     * TC-02: An attacker who modifies balanceSnapshot or expiresAt must fail signature verification,
     * because the HMAC is computed over the canonical payload including those fields.
     */
    private static void testTamperedQrPayloadRejected() {
        PulseVenueService service = new PulseVenueService();
        QrToken valid = service.generateQrToken("w_attendee_01", 120, BASE_TIME);

        // Attacker inflates balance from 180 → 9999 but reuses original signature
        QrToken tamperedBalance = new QrToken(
                valid.walletId(), valid.ownerName(),
                9999.00,                        // ← tampered
                valid.currency(), valid.nonce(),
                valid.issuedAt(), valid.expiresAt(), valid.signature());

        OfflineVerificationResult r1 = service.verifyQrTokenOffline(tamperedBalance, 50.0, 0.0, BASE_TIME + 5);
        assertFalse(r1.valid(), "Tampered balanceSnapshot must fail HMAC check");
        assertContains(r1.reason(), "Invalid HMAC signature", "Rejection must cite HMAC mismatch");

        // Attacker extends expiry by 1 hour without re-signing
        QrToken tamperedExpiry = new QrToken(
                valid.walletId(), valid.ownerName(), valid.balanceSnapshot(),
                valid.currency(), valid.nonce(),
                valid.issuedAt(), valid.expiresAt() + 3600L, // ← tampered
                valid.signature());

        OfflineVerificationResult r2 = service.verifyQrTokenOffline(tamperedExpiry, 10.0, 0.0, BASE_TIME + 5);
        assertFalse(r2.valid(), "Tampered expiresAt must fail HMAC check");
        assertContains(r2.reason(), "Invalid HMAC signature", "Extended expiry must trigger HMAC mismatch");
    }

    /**
     * TC-03: Token checked exactly at expiresAt is still valid; 1 second past it is rejected.
     */
    private static void testExpiredQrTokenRejected() {
        PulseVenueService service = new PulseVenueService();
        QrToken token = service.generateQrToken("w_attendee_01", 60, BASE_TIME);

        // Exact boundary (checkTimestamp == expiresAt) must still pass
        OfflineVerificationResult onBoundary = service.verifyQrTokenOffline(token, 10.0, 0.0, BASE_TIME + 60);
        assertTrue(onBoundary.valid(), "Token checked exactly at expiresAt must still be valid");

        // 1 second past expiry must be rejected
        OfflineVerificationResult expired = service.verifyQrTokenOffline(token, 10.0, 0.0, BASE_TIME + 61);
        assertFalse(expired.valid(), "Token checked after expiresAt must be rejected");
        assertContains(expired.reason(), "expired", "Rejection reason must mention expiration");
    }

    /**
     * TC-05: Two offline transactions sync correctly: attendee is debited, bar is credited,
     * and the total sum of all wallet balances stays identical (double-entry conservation).
     */
    private static void testDoubleEntryBalanceConservation() {
        PulseVenueService service = new PulseVenueService();
        double totalBefore = walletSum(service);
        QrToken token = service.generateQrToken("w_attendee_01", 300, BASE_TIME);

        List<OfflineTransaction> batch = List.of(
                new OfflineTransaction("tx_sync_01", "w_attendee_01", "w_bar_north", 25.00, token, BASE_TIME + 5),
                new OfflineTransaction("tx_sync_02", "w_attendee_01", "w_bar_north", 15.50, token, BASE_TIME + 10)
        );

        SyncBatchResult result = service.syncOfflineBatch("POS_NORTH_01", batch, BASE_TIME + 20);
        assertEquals(2, result.syncedCount(),        "Both transactions must sync successfully");
        assertEquals(0, result.skippedDuplicates(),  "No duplicates expected in first sync");
        assertTrue(result.rejectedReasons().isEmpty(), "No rejection reasons expected");

        assertClose(139.50, walletBalance(service, "w_attendee_01"), 1e-6, "Attendee debited: 180.00 - 40.50 = 139.50");
        assertClose(5040.50, walletBalance(service, "w_bar_north"),  1e-6, "Bar credited: 5000.00 + 40.50 = 5040.50");
        assertClose(totalBefore, walletSum(service), 1e-6, "Total wallet sum must be invariant (double-entry conservation)");
    }

    /**
     * TC-06: Submitting the same batch twice must settle exactly once.
     * The second submission reports skippedDuplicates == 1 and balances are unchanged.
     */
    private static void testDuplicateBatchSyncIdempotency() {
        PulseVenueService service = new PulseVenueService();
        QrToken token = service.generateQrToken("w_attendee_01", 300, BASE_TIME);
        OfflineTransaction tx = new OfflineTransaction("tx_idem_901", "w_attendee_01", "w_bar_north", 20.00, token, BASE_TIME + 5);

        SyncBatchResult first = service.syncOfflineBatch("POS_NORTH_01", List.of(tx), BASE_TIME + 10);
        assertEquals(1, first.syncedCount(),       "First sync must settle 1 transaction");
        assertEquals(0, first.skippedDuplicates(), "First sync must have 0 skipped duplicates");

        // Client retry (e.g. timeout / network error)
        SyncBatchResult retry = service.syncOfflineBatch("POS_NORTH_01", List.of(tx), BASE_TIME + 15);
        assertEquals(0, retry.syncedCount(),       "Retry sync must settle 0 transactions");
        assertEquals(1, retry.skippedDuplicates(), "Retry must report skippedDuplicates == 1");

        // Balance adjusted only once
        assertClose(160.00, walletBalance(service, "w_attendee_01"), 1e-6, "Attendee debited only once");
        assertClose(5020.00, walletBalance(service, "w_bar_north"),  1e-6, "Bar credited only once");
    }

    /**
     * TC-07: When a zone's occupancy reaches 85%, it must be marked CONGESTED and a 20% FlashDiscount
     * must be generated targeting the least-occupied NORMAL bar zone.
     */
    private static void testBottleneckAndFlashDiscountTrigger() {
        PulseVenueService service = new PulseVenueService();

        // Below threshold everywhere → no discounts
        List<FlashDiscount> noDiscounts = service.updateZoneTelemetry(Map.of(
                "Z_STAGE_MAIN", 38249,  // 84.997% of 45,000 — just under
                "Z_BAR_NORTH",  12000,  // 80.0%
                "Z_BAR_SOUTH",   4400   // 24.4%
        ), BASE_TIME);
        assertTrue(noDiscounts.isEmpty(), "No flash discount below 85% occupancy threshold");

        // Exactly 85% at Main Stage + 86.7% at North Bar → Z_BAR_SOUTH (24.4%) is relief target
        List<FlashDiscount> discounts = service.updateZoneTelemetry(Map.of(
                "Z_STAGE_MAIN", 38250,  // 85.0% → CONGESTED
                "Z_BAR_NORTH",  13000,  // 86.7% → CONGESTED
                "Z_BAR_SOUTH",   4400   // 24.4% → NORMAL (relief bar)
        ), BASE_TIME + 5);

        assertFalse(discounts.isEmpty(), "Congested zone at 85% must emit a FlashDiscount");
        FlashDiscount promo = discounts.getFirst();
        assertEquals("Z_BAR_SOUTH", promo.targetZoneId(), "Relief promo must target lowest-occupancy NORMAL bar");
        assertEquals(20, promo.discountPercent(), "Flash discount must be exactly 20%");
        assertTrue(promo.active(), "Generated flash discount must be marked active");

        H3Zone stageZone = service.getZones().stream()
                .filter(z -> "Z_STAGE_MAIN".equals(z.zoneId()))
                .findFirst().orElseThrow();
        assertEquals("CONGESTED", stageZone.status(), "Zone at >= 85% must be marked CONGESTED");
    }

    /**
     * TC-10: A transaction with a blank or whitespace-only idempotencyKey must be rejected
     * and must NOT modify any wallet balance.
     */
    private static void testBlankIdempotencyKeyRejected() {
        PulseVenueService service = new PulseVenueService();
        double totalBefore = walletSum(service);

        OfflineTransaction badTx = new OfflineTransaction("   ", "w_attendee_01", "w_bar_north", 30.0, null, BASE_TIME);
        SyncBatchResult result = service.syncOfflineBatch("POS_NORTH_01", List.of(badTx), BASE_TIME + 5);

        assertEquals(0, result.syncedCount(),            "Blank idempotencyKey must not sync");
        assertEquals(1, result.rejectedReasons().size(), "One rejection reason must be returned");
        assertClose(totalBefore, walletSum(service), 1e-6, "No wallet balance must change on rejection");
    }

    // --- Helpers ---
    private static double walletSum(PulseVenueService svc) {
        return svc.getWallets().stream().mapToDouble(Wallet::balance).sum();
    }

    private static double walletBalance(PulseVenueService svc, String walletId) {
        return svc.getWallets().stream()
                .filter(w -> walletId.equals(w.walletId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Wallet not found: " + walletId))
                .balance();
    }
}
