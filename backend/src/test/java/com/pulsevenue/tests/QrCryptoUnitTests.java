package com.pulsevenue.tests;

import com.pulsevenue.model.Models.OfflineVerificationResult;
import com.pulsevenue.model.Models.QrToken;
import com.pulsevenue.service.PulseVenueService;

import static com.pulsevenue.tests.TestAssert.*;

/**
 * ISS-08 (QA / Tester): Unit tests for HMAC-SHA256 QR token generation and offline verification.
 * Test Cases: TC-01, TC-04, TC-09
 */
public final class QrCryptoUnitTests {

    // A fixed reference timestamp for deterministic tests (seconds since epoch)
    private static final long BASE_TIME = 1762250000L;

    private QrCryptoUnitTests() {}

    public static void runAll() {
        System.out.println("\n--- [ISS-08] QR Crypto & Offline Verification Unit Tests ---");
        runTest("TC-01", "Valid QR token generation & offline verification",   QrCryptoUnitTests::testValidQrGenerationAndVerification);
        runTest("TC-04", "Reject order exceeding remaining offline balance",    QrCryptoUnitTests::testInsufficientOfflineBalance);
        runTest("TC-09", "Reject null/zero/negative order parameters",         QrCryptoUnitTests::testInvalidVerificationParameters);
    }

    /**
     * TC-01: A freshly generated QR token must have correct fields, a valid 64-hex signature,
     * rotating nonces between calls, and must pass offline verification for a valid order amount.
     */
    private static void testValidQrGenerationAndVerification() {
        PulseVenueService service = new PulseVenueService();
        QrToken token1 = service.generateQrToken("w_attendee_01", 60, BASE_TIME);
        QrToken token2 = service.generateQrToken("w_attendee_01", 60, BASE_TIME);

        assertTrue(token1 != null, "Generated QR token must not be null");
        assertEquals("w_attendee_01", token1.walletId(), "Wallet ID must match request");
        assertClose(180.00, token1.balanceSnapshot(), 1e-6, "Balance snapshot must be 180.00 RON");
        assertEquals("RON", token1.currency(), "Currency must be RON");
        assertEquals(BASE_TIME,      token1.issuedAt(),  "issuedAt must equal the request timestamp");
        assertEquals(BASE_TIME + 60, token1.expiresAt(), "expiresAt must equal issuedAt + ttlSeconds");
        assertTrue(token1.nonce() != null && !token1.nonce().isBlank(), "Nonce must be non-blank");
        assertTrue(token1.signature() != null && token1.signature().matches("^[0-9a-f]{64}$"),
                "HMAC-SHA256 signature must be 64 lowercase hex chars");

        // "Rotating" means each token has a unique nonce → unique signature
        assertFalse(token1.nonce().equals(token2.nonce()),         "Consecutive tokens must rotate nonce");
        assertFalse(token1.signature().equals(token2.signature()), "Consecutive tokens must produce distinct signatures");

        // Offline verification: spend 25.50 RON with 0 already spent, still within TTL
        OfflineVerificationResult result = service.verifyQrTokenOffline(token1, 25.50, 0.0, BASE_TIME + 10);
        assertTrue(result.valid(), "Valid token within TTL and balance must verify offline");
        assertClose(154.50, result.remainingOfflineBalance(), 1e-6, "Remaining balance = 180.00 - 25.50 = 154.50");
    }

    /**
     * TC-04: When localAlreadySpent + orderAmount exceeds the balance snapshot, the verification
     * must reject the payment, report the remaining available amount, and succeed on the exact boundary.
     */
    private static void testInsufficientOfflineBalance() {
        PulseVenueService service = new PulseVenueService();
        QrToken token = service.generateQrToken("w_attendee_01", 120, BASE_TIME);

        // 150.00 spent + 50.00 order = 200.00 > 180.00 snapshot → REJECT
        OfflineVerificationResult rejected = service.verifyQrTokenOffline(token, 50.00, 150.00, BASE_TIME + 5);
        assertFalse(rejected.valid(), "Order exceeding remaining balance must be rejected");
        assertContains(rejected.reason(), "Insufficient balance", "Rejection reason must cite insufficient balance");
        assertClose(30.00, rejected.remainingOfflineBalance(), 1e-6, "Remaining available = 180.00 - 150.00 = 30.00");

        // Exact remaining balance (30.00) must succeed
        OfflineVerificationResult exact = service.verifyQrTokenOffline(token, 30.00, 150.00, BASE_TIME + 5);
        assertTrue(exact.valid(), "Spending exactly the remaining balance must succeed");
        assertClose(0.0, exact.remainingOfflineBalance(), 1e-6, "Zero remaining after spending all funds");
    }

    /**
     * TC-09: Null token, zero order amount, and negative order amount must all be safely rejected.
     */
    private static void testInvalidVerificationParameters() {
        PulseVenueService service = new PulseVenueService();
        QrToken token = service.generateQrToken("w_attendee_01", 60, BASE_TIME);

        OfflineVerificationResult nullToken   = service.verifyQrTokenOffline(null,  10.0,  0.0, BASE_TIME);
        OfflineVerificationResult zeroAmount  = service.verifyQrTokenOffline(token,  0.0,  0.0, BASE_TIME);
        OfflineVerificationResult negAmount   = service.verifyQrTokenOffline(token, -15.0, 0.0, BASE_TIME);

        assertFalse(nullToken.valid(),  "Null QR token must be rejected");
        assertFalse(zeroAmount.valid(), "Zero order amount must be rejected");
        assertFalse(negAmount.valid(),  "Negative order amount must be rejected");
    }
}
