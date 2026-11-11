package com.pulsevenue.simulator;

import com.pulsevenue.service.PulseVenueService;
import com.pulsevenue.simulator.CrowdSimulator;
import com.pulsevenue.simulator.CrowdSimulator.SimulationTickReport;

public class SimulatorTest {

    public static void main(String[] args) {
        PulseVenueService service = new PulseVenueService();
        CrowdSimulator simulator = new CrowdSimulator(service);

        System.out.println("=== STARTING PULSEVENUE 80,000 AGENT SIMULATOR ===");

        // Run Tick 1: Standard crowd distribution
        SimulationTickReport report1 = simulator.runTick(80000, null);
        printReport(report1);

        // Run Tick 2: Surge at Main Stage Arena ("Z_STAGE_MAIN")
        SimulationTickReport report2 = simulator.runTick(80000, "Z_STAGE_MAIN");
        printReport(report2);

        // Run Tick 3: Surge at North Main Bar ("Z_BAR_NORTH")
        SimulationTickReport report3 = simulator.runTick(80000, "Z_BAR_NORTH");
        printReport(report3);
    }

    private static void printReport(SimulationTickReport r) {
        System.out.printf("%n--- TICK #%d REPORT ---%n", r.tickNumber());
        System.out.printf("Agents Simulated: %,d%n", r.totalAgentsSimulated());
        System.out.printf("Execution Time:   %d ms%n", r.elapsedMillis());
        System.out.printf("Synced POS Txns:  %d%n", r.sampleBatchTransactionsSynced());
        System.out.printf("Discounts Issued: %d%n", r.activeDiscountsCount());
        System.out.println("Bottlenecks Triggered: " + (r.bottlenecksTriggered().isEmpty() ? "None" : r.bottlenecksTriggered()));
    }
}