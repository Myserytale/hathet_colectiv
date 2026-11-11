package com.pulsevenue.simulator;

import com.pulsevenue.model.Models.*;
import com.pulsevenue.service.PulseVenueService;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class CrowdSimulator {

    private final PulseVenueService service;
    private final AtomicInteger tickCounter = new AtomicInteger(0);

    public CrowdSimulator(PulseVenueService service) {
        this.service = service;
    }

    public record SimulationTickReport(
            int tickNumber,
            int totalAgentsSimulated,
            long elapsedMillis,
            List<String> bottlenecksTriggered,
            int activeDiscountsCount,
            int sampleBatchTransactionsSynced
    ) {}

    /**
     * ISS-19: Simulates crowd movement, telemetry updates, bottleneck detection,
     * flash discount generation, and synthetic offline POS transaction sync.
     */
    public SimulationTickReport runTick(int totalAgents, String surgeZoneId) {
        long startTime = System.currentTimeMillis();
        int currentTick = tickCounter.incrementAndGet();
        long now = Instant.now().getEpochSecond();

        List<H3Zone> zones = service.getZones();
        if (zones.isEmpty()) {
            return new SimulationTickReport(currentTick, totalAgents, 0L, List.of(), 0, 0);
        }

        // 1. Distribute 80,000 agents dynamically across venue H3 zones
        Map<String, Integer> zoneCounts = distributeAgents(zones, totalAgents, surgeZoneId);

        // 2. Submit telemetry tick & receive triggered flash discounts
        List<FlashDiscount> activeDiscounts = service.updateZoneTelemetry(zoneCounts, now);

        // 3. Inspect updated zones to report congested bottlenecks
        List<String> bottlenecksTriggered = service.getZones().stream()
                .filter(z -> "CONGESTED".equalsIgnoreCase(z.status()) || z.occupancyRatio() >= z.bottleneckThreshold())
                .map(z -> String.format(Locale.US, "%s [%s] (Occupancy: %.1f%%)",
                        z.name(), z.zoneId(), z.occupancyRatio() * 100))
                .toList();

        // 4. Generate & sync a batch of synthetic offline POS transactions
        int sampleBatchTransactionsSynced = simulateOfflineTransactionBatch(now);

        long elapsedMillis = System.currentTimeMillis() - startTime;

        return new SimulationTickReport(
                currentTick,
                totalAgents,
                elapsedMillis,
                bottlenecksTriggered,
                activeDiscounts.size(),
                sampleBatchTransactionsSynced
        );
    }

    /**
     * Distributes agents across zones. If surgeZoneId is set, concentrates 65% of agents
     * into that targeted zone to trigger crowd bottlenecks.
     */
    private Map<String, Integer> distributeAgents(List<H3Zone> zones, int totalAgents, String surgeZoneId) {
        Map<String, Integer> counts = new HashMap<>();

        boolean hasSurge = surgeZoneId != null && !surgeZoneId.isBlank()
                && zones.stream().anyMatch(z -> z.zoneId().equalsIgnoreCase(surgeZoneId) || z.h3Index().equalsIgnoreCase(surgeZoneId));

        if (hasSurge) {
            int surgeAgents = (int) (totalAgents * 0.65);
            int remainingAgents = totalAgents - surgeAgents;

            List<H3Zone> otherZones = zones.stream()
                    .filter(z -> !z.zoneId().equalsIgnoreCase(surgeZoneId) && !z.h3Index().equalsIgnoreCase(surgeZoneId))
                    .toList();

            long totalOtherCapacity = otherZones.stream().mapToLong(H3Zone::capacity).sum();

            for (H3Zone zone : zones) {
                if (zone.zoneId().equalsIgnoreCase(surgeZoneId) || zone.h3Index().equalsIgnoreCase(surgeZoneId)) {
                    counts.put(zone.zoneId(), surgeAgents);
                } else if (totalOtherCapacity > 0) {
                    double ratio = (double) zone.capacity() / totalOtherCapacity;
                    counts.put(zone.zoneId(), (int) (remainingAgents * ratio));
                } else {
                    counts.put(zone.zoneId(), remainingAgents / Math.max(1, otherZones.size()));
                }
            }
        } else {
            long totalCapacity = zones.stream().mapToLong(H3Zone::capacity).sum();
            for (H3Zone zone : zones) {
                double ratio = totalCapacity > 0 ? (double) zone.capacity() / totalCapacity : 1.0 / zones.size();
                counts.put(zone.zoneId(), (int) (totalAgents * ratio));
            }
        }

        return counts;
    }

    /**
     * Generates 50 synthetic offline POS transactions per tick (QR generation -> offline HMAC verify -> batch sync).
     */
    private int simulateOfflineTransactionBatch(long now) {
        int batchSize = 50;
        List<OfflineTransaction> transactions = new ArrayList<>();

        for (int i = 0; i < batchSize; i++) {
            String walletId = "w_attendee_01";
            QrToken token = service.generateQrToken(walletId, 300, now);

            double amount = 10.0 + ThreadLocalRandom.current().nextDouble(30.0);
            OfflineVerificationResult verification = service.verifyQrTokenOffline(token, amount, 0.0, now);

            if (verification.valid()) {
                String idempotencyKey = "tx_sim_" + UUID.randomUUID().toString();
                transactions.add(new OfflineTransaction(
                        idempotencyKey,
                        walletId,
                        "w_bar_north",
                        amount,
                        token,
                        now
                ));
            }
        }

        SyncBatchResult result = service.syncOfflineBatch("POS_SIMULATOR_01", transactions, now);
        return result.syncedCount();
    }
}