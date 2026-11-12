package com.pulsevenue;

import com.pulsevenue.model.Models.QrToken;
import com.pulsevenue.service.PulseVenueService;
import com.pulsevenue.tests.QrCryptoUnitTests;
import com.pulsevenue.tests.SecurityAndSyncTests;
import com.pulsevenue.tests.SimulatorBenchmarkTests;
import com.pulsevenue.tests.TestAssert;

/**
 * PulseVenue QA Automated Test Suite Runner.
 *
 * Issues covered:
 *   ISS-08 — TC-01, TC-04, TC-09  (QR Crypto & Offline Verification Unit Tests)
 *   ISS-17 — TC-02, TC-03, TC-05..TC-07, TC-10  (Security, Idempotency & Bottleneck Tests)
 *   ISS-25 — TC-08  (80,000-Agent Stress Benchmark)
 *
 * Run via CI:   java -cp backend/target/classes com.pulsevenue.PulseVenueTest
 */
public class PulseVenueTest {

    /** Original skeleton smoke check kept for CI backward compatibility. */
    public static void testSkeletonContractCompilesAndRuns() {
        PulseVenueService service = new PulseVenueService();
        QrToken token = service.generateQrToken("w_attendee_01", 60, 1762250000L);
        TestAssert.assertTrue(token != null && "w_attendee_01".equals(token.walletId()),
                "Skeleton QR token contract check failed");
    }

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("  PulseVenue QA Test Suite  (ISS-08 · ISS-17 · ISS-25)");
        System.out.println("============================================================");

        TestAssert.resetCounters();

        // Smoke test (always first)
        TestAssert.runTest("SMOKE", "Architectural skeleton contract smoke check",
                PulseVenueTest::testSkeletonContractCompilesAndRuns);

        // ISS-08 — TC-01, TC-04, TC-09
        QrCryptoUnitTests.runAll();

        // ISS-17 — TC-02, TC-03, TC-05..TC-07, TC-10
        SecurityAndSyncTests.runAll();

        // ISS-25 — TC-08
        SimulatorBenchmarkTests.runAll();

        // --- Summary ---
        int passed = TestAssert.getPassedCount();
        int failed = TestAssert.getFailedCount();
        System.out.println("\n============================================================");
        System.out.printf("  RESULT: %d PASSED, %d FAILED%n", passed, failed);
        System.out.println("============================================================");

        if (failed > 0) {
            System.exit(1); // Makes CI pipeline mark the build as failed
        }
    }
}
