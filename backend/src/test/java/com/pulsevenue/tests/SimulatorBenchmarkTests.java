package com.pulsevenue.tests;

import com.pulsevenue.model.Models.Wallet;
import com.pulsevenue.service.PulseVenueService;
import com.pulsevenue.simulator.CrowdSimulator;
import com.pulsevenue.simulator.CrowdSimulator.SimulationTickReport;

import java.util.ArrayList;
import java.util.List;

import static com.pulsevenue.tests.TestAssert.*;

/**
 * ISS-25 (QA / Tester): 80,000-Agent Crowd & Commerce Stress Benchmark.
 * Test Case: TC-08
 */
public final class SimulatorBenchmarkTests {

    private static final int  TARGET_AGENTS = 80_000;
    private static final long MAX_TICK_MS   = 2000L;

    private SimulatorBenchmarkTests() {}

    public static void runAll() {
        System.out.println("\n--- [ISS-25] 80,000-Agent Stress Benchmark (TC-08) ---");
        runTest("TC-08", "80k-agent ticks < 2000ms, bottlenecks detected, 0 ledger drift",
                SimulatorBenchmarkTests::testEightyThousandAgentStressBenchmark);
    }

    /**
     * TC-08: Runs 3 consecutive 80,000-agent simulator ticks:
     *   Tick 1 — standard crowd distribution (no surge)
     *   Tick 2 — 65% agents surged into Z_STAGE_MAIN
     *   Tick 3 — 65% agents surged into Z_BAR_NORTH
     *
     * Assertions:
     *  - Every tick processes exactly 80,000 agents
     *  - Every tick completes in < 2000ms (NFR-04 / NFR-05)
     *  - Surge ticks trigger at least one bottleneck and at least one flash discount
     *  - Tick 1 syncs >= 50 POS transactions total (wallet may drain in later ticks)
     *  - Total wallet balance is invariant across all ticks (0 ledger discrepancy)
     */
    private static void testEightyThousandAgentStressBenchmark() {
        PulseVenueService service = new PulseVenueService();
        CrowdSimulator simulator  = new CrowdSimulator(service);

        double initialTotalBalance = service.getWallets().stream().mapToDouble(Wallet::balance).sum();

        List<SimulationTickReport> reports = new ArrayList<>();
        reports.add(simulator.runTick(TARGET_AGENTS, null));
        reports.add(simulator.runTick(TARGET_AGENTS, "Z_STAGE_MAIN"));
        reports.add(simulator.runTick(TARGET_AGENTS, "Z_BAR_NORTH"));

        System.out.println();
        System.out.printf("  %-6s %-11s %-12s %-13s %-11s%n",
                "Tick", "Agents", "Latency(ms)", "SyncedTxns", "Discounts");
        System.out.println("  " + "-".repeat(60));

        int totalSynced = 0;
        for (SimulationTickReport r : reports) {
            System.out.printf("  %-6d %-11s %-12d %-13d %-11d  Bottlenecks: %s%n",
                    r.tickNumber(),
                    String.format("%,d", r.totalAgentsSimulated()),
                    r.elapsedMillis(),
                    r.sampleBatchTransactionsSynced(),
                    r.activeDiscountsCount(),
                    r.bottlenecksTriggered().isEmpty() ? "none" : r.bottlenecksTriggered());

            assertEquals(TARGET_AGENTS, r.totalAgentsSimulated(),
                    "Tick " + r.tickNumber() + " must simulate exactly 80,000 agents");
            assertTrue(r.elapsedMillis() < MAX_TICK_MS,
                    "Tick " + r.tickNumber() + " exceeded 2000ms SLA: " + r.elapsedMillis() + "ms");

            totalSynced += r.sampleBatchTransactionsSynced();
        }

        // Tick 1 syncs 50 POS txns; later ticks may drain wallet — total must be >= 50
        assertTrue(totalSynced >= 50,
                "At least 50 POS txns must be synced across all ticks, got: " + totalSynced);

        // Surge ticks must detect bottlenecks and emit flash discounts
        assertFalse(reports.get(1).bottlenecksTriggered().isEmpty(),
                "Tick 2 (Z_STAGE_MAIN surge) must detect at least one bottleneck");
        assertTrue(reports.get(1).activeDiscountsCount() > 0,
                "Tick 2 surge must trigger at least one flash discount");
        assertFalse(reports.get(2).bottlenecksTriggered().isEmpty(),
                "Tick 3 (Z_BAR_NORTH surge) must detect at least one bottleneck");

        // Double-entry conservation: total balance must be identical before and after all ticks
        double finalTotalBalance = service.getWallets().stream().mapToDouble(Wallet::balance).sum();
        assertClose(initialTotalBalance, finalTotalBalance, 1e-6,
                "Total wallet balance must be conserved across all 3 ticks (0 ledger discrepancy)");

        System.out.println();
        System.out.printf("  Ledger balance before: %.2f RON%n", initialTotalBalance);
        System.out.printf("  Ledger balance after:  %.2f RON%n", finalTotalBalance);
        System.out.printf("  Drift: %.10f RON  ✓%n", Math.abs(initialTotalBalance - finalTotalBalance));
    }
}
