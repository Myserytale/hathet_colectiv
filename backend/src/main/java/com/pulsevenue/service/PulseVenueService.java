package com.pulsevenue.service;

import com.pulsevenue.model.Models.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PulseVenueService {

    public static final String VENUE_HMAC_SECRET = "pulsevenue-offline-signing-secret-2026";

    // In-memory state storage
    private final Map<String, Wallet> walletStore = new ConcurrentHashMap<>();
    private final Map<String, H3Zone> zoneStore = new ConcurrentHashMap<>();
    private final Set<String> processedIdempotencyKeys = ConcurrentHashMap.newKeySet();
    private final List<LedgerEntry> ledger = Collections.synchronizedList(new ArrayList<>());

    public PulseVenueService() {
        long now = Instant.now().getEpochSecond();
        // Seed default wallets
        walletStore.put("w_attendee_01", new Wallet("w_attendee_01", "Alex Popescu (Attendee)", "ATTENDEE", 180.00, "RON", now));
        walletStore.put("w_bar_north", new Wallet("w_bar_north", "North Arena Bar POS", "BARTENDER_POS", 5000.00, "RON", now));

        // Seed default H3 Zones
        zoneStore.put("Z_STAGE_MAIN", new H3Zone("Z_STAGE_MAIN", "8a2a1072b59ffff", "Main Stage Arena", "STAGE", 45000, 28500, 0.85, "ELEVATED", 0.633, now));
        zoneStore.put("Z_BAR_NORTH", new H3Zone("Z_BAR_NORTH", "8a2a1072b51ffff", "North Main Bar", "BAR", 15000, 11800, 0.85, "ELEVATED", 0.787, now));
        zoneStore.put("Z_BAR_SOUTH", new H3Zone("Z_BAR_SOUTH", "8a2a1072b12ffff", "South Chill Bar", "BAR", 18000, 4400, 0.85, "NORMAL", 0.244, now));
    }

    /**
     * ISS-05: Generates a signed QR token using HMAC-SHA256 over canonical payload format ADR-001.
     */
    public QrToken generateQrToken(String walletId, int ttlSeconds, long nowEpochSeconds) {
        Wallet wallet = walletStore.getOrDefault(walletId,
                new Wallet(walletId, "Dummy_Attendee", "ATTENDEE", 180.00, "RON", nowEpochSeconds));

        String nonce = UUID.randomUUID().toString().substring(0, 8);
        long expiresAt = nowEpochSeconds + ttlSeconds;

        String canonicalPayload = buildCanonicalPayload(
                wallet.walletId(),
                wallet.balance(),
                wallet.currency(),
                nonce,
                nowEpochSeconds,
                expiresAt
        );

        String signature = computeHmacSha256(canonicalPayload, VENUE_HMAC_SECRET);

        return new QrToken(
                wallet.walletId(),
                wallet.ownerName(),
                wallet.balance(),
                wallet.currency(),
                nonce,
                nowEpochSeconds,
                expiresAt,
                signature
        );
    }

    /**
     * ISS-05: Verifies QR token offline: HMAC validity, expiration, and offline balance check.
     */
    public OfflineVerificationResult verifyQrTokenOffline(
            QrToken token, double orderAmount, double localAlreadySpent, long checkTimestamp) {

        if (token == null || orderAmount <= 0) {
            return new OfflineVerificationResult(false, "Invalid parameters: token null or order amount <= 0", 0.0);
        }

        // 1. Expiration Check
        if (checkTimestamp > token.expiresAt()) {
            return new OfflineVerificationResult(false, "Token expired", 0.0);
        }

        // 2. Cryptographic HMAC Signature Verification
        String canonicalPayload = buildCanonicalPayload(
                token.walletId(),
                token.balanceSnapshot(),
                token.currency(),
                token.nonce(),
                token.issuedAt(),
                token.expiresAt()
        );

        String expectedSignature = computeHmacSha256(canonicalPayload, VENUE_HMAC_SECRET);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
                token.signature().getBytes(StandardCharsets.UTF_8))) {
            return new OfflineVerificationResult(false, "Invalid HMAC signature", 0.0);
        }

        // 3. Balance Check (Snapshot - Offline Local Spent - Order Amount)
        double remainingBalance = token.balanceSnapshot() - localAlreadySpent - orderAmount;
        if (remainingBalance < 0) {
            return new OfflineVerificationResult(
                    false,
                    String.format(Locale.US, "Insufficient balance. Available: %.2f RON", token.balanceSnapshot() - localAlreadySpent),
                    Math.max(0.0, token.balanceSnapshot() - localAlreadySpent)
            );
        }

        return new OfflineVerificationResult(true, "Offline verification successful", remainingBalance);
    }

    /**
     * ISS-10: Atomic double-entry ledger batch synchronization with idempotency deduplication.
     */
    public SyncBatchResult syncOfflineBatch(
            String posTerminalId, List<OfflineTransaction> transactions, long syncTimestamp) {

        if (transactions == null || transactions.isEmpty()) {
            return new SyncBatchResult(posTerminalId, 0, 0, List.of());
        }

        int syncedCount = 0;
        int skippedDuplicates = 0;
        List<String> rejectedReasons = new ArrayList<>();

        for (OfflineTransaction tx : transactions) {
            if (tx.idempotencyKey() == null || tx.idempotencyKey().isBlank()) {
                rejectedReasons.add("Missing idempotency key for transaction debiting " + tx.debitWalletId());
                continue;
            }

            // Deduplication Check
            if (processedIdempotencyKeys.contains(tx.idempotencyKey())) {
                skippedDuplicates++;
                continue;
            }

            // Record into Ledger & Double-Entry Balance Adjustment
            processedIdempotencyKeys.add(tx.idempotencyKey());
            ledger.add(new LedgerEntry(
                    ledger.size() + 1,
                    tx.idempotencyKey(),
                    tx.debitWalletId(),
                    tx.creditWalletId(),
                    tx.amount(),
                    posTerminalId,
                    tx.qrToken() != null ? tx.qrToken().nonce() : "N/A",
                    tx.createdOfflineAt(),
                    syncTimestamp
            ));

            // Adjust Account Balance in System Ledger
            adjustWalletBalance(tx.debitWalletId(), -tx.amount(), syncTimestamp);
            adjustWalletBalance(tx.creditWalletId(), tx.amount(), syncTimestamp);

            syncedCount++;
        }

        return new SyncBatchResult(posTerminalId, syncedCount, skippedDuplicates, rejectedReasons);
    }

    /**
     * ISS-12: Telemetry headcount update, crowd bottleneck detection (occupancy >= 0.85),
     * and dynamic 20% FlashDiscount triggering at the nearest NORMAL bar/food zone.
     */
    public List<FlashDiscount> updateZoneTelemetry(Map<String, Integer> newZoneCounts, long nowTimestamp) {
        if (newZoneCounts == null || newZoneCounts.isEmpty()) {
            return List.of();
        }

        List<FlashDiscount> generatedDiscounts = new ArrayList<>();

        // 1. Update headcount and recalculate occupancy metrics
        for (Map.Entry<String, Integer> entry : newZoneCounts.entrySet()) {
            String key = entry.getKey();
            int newCount = entry.getValue();

            H3Zone existingZone = findZone(key);
            if (existingZone != null) {
                double newOccupancy = (double) newCount / existingZone.capacity();
                String newStatus = newOccupancy >= existingZone.bottleneckThreshold() ? "CONGESTED" : "NORMAL";

                H3Zone updatedZone = new H3Zone(
                        existingZone.zoneId(),
                        existingZone.h3Index(),
                        existingZone.name(),
                        existingZone.zoneType(),
                        existingZone.capacity(),
                        newCount,
                        existingZone.bottleneckThreshold(),
                        newStatus,
                        newOccupancy,
                        nowTimestamp
                );
                zoneStore.put(existingZone.zoneId(), updatedZone);
            }
        }

        // 2. Identify Congested Zones and pair with nearest NORMAL Bar/Food Zone
        List<H3Zone> congestedZones = zoneStore.values().stream()
                .filter(z -> z.occupancyRatio() >= z.bottleneckThreshold())
                .toList();

        List<H3Zone> normalBars = zoneStore.values().stream()
                .filter(z -> ("BAR".equalsIgnoreCase(z.zoneType()) || "FOOD".equalsIgnoreCase(z.zoneType()))
                        && "NORMAL".equalsIgnoreCase(z.status()))
                .sorted(Comparator.comparingDouble(H3Zone::occupancyRatio))
                .toList();

        for (H3Zone congested : congestedZones) {
            if (!normalBars.isEmpty()) {
                H3Zone targetBar = normalBars.getFirst();
                String discountId = "FD_" + congested.zoneId() + "_" + targetBar.zoneId() + "_" + nowTimestamp;
                String message = String.format("High crowd at %s! Head to %s for 20%% OFF drinks!",
                        congested.name(), targetBar.name());

                generatedDiscounts.add(new FlashDiscount(
                        discountId,
                        congested.zoneId(),
                        targetBar.zoneId(),
                        20,
                        message,
                        true,
                        nowTimestamp
                ));
            }
        }

        return generatedDiscounts;
    }

    // --- Auxiliary Service Helpers ---

    private H3Zone findZone(String key) {
        if (zoneStore.containsKey(key)) return zoneStore.get(key);
        return zoneStore.values().stream()
                .filter(z -> z.h3Index().equalsIgnoreCase(key))
                .findFirst()
                .orElse(null);
    }

    private void adjustWalletBalance(String walletId, double amountDelta, long timestamp) {
        Wallet wallet = walletStore.get(walletId);
        if (wallet != null) {
            walletStore.put(walletId, new Wallet(
                    wallet.walletId(),
                    wallet.ownerName(),
                    wallet.role(),
                    wallet.balance() + amountDelta,
                    wallet.currency(),
                    timestamp
            ));
        }
    }

    private String buildCanonicalPayload(String walletId, double balance, String currency, String nonce, long issuedAt, long expiresAt) {
        return String.format(Locale.GERMANY, "%s|%.2f|%s|%s|%d|%d", walletId, balance, currency, nonce, issuedAt, expiresAt);
    }

    private String computeHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate HMAC-SHA256 signature", e);
        }
    }

    public List<Wallet> getWallets() {
        return new ArrayList<>(walletStore.values());
    }

    public List<H3Zone> getZones() {
        return new ArrayList<>(zoneStore.values());
    }
}