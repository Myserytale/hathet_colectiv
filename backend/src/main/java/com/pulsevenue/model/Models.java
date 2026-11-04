package com.pulsevenue.model;

import java.util.List;

/**
 * Barebone Java 21 domain models for PulseVenue.
 * Owner Roles: Software Architect, Backend Developer, Database Engineer.
 */
public final class Models {

    private Models() {}

    public record Wallet(
            String walletId,
            String ownerName,
            String role,
            double balance,
            String currency,
            long updatedAt
    ) {}

    public record QrToken(
            String walletId,
            String ownerName,
            double balanceSnapshot,
            String currency,
            String nonce,
            long issuedAt,
            long expiresAt,
            String signature
    ) {}

    public record OfflineVerificationResult(
            boolean valid,
            String reason,
            double remainingOfflineBalance
    ) {}

    public record OfflineTransaction(
            String idempotencyKey,
            String debitWalletId,
            String creditWalletId,
            double amount,
            QrToken qrToken,
            long createdOfflineAt
    ) {}

    public record LedgerEntry(
            long entryId,
            String idempotencyKey,
            String debitWalletId,
            String creditWalletId,
            double amount,
            String posTerminalId,
            String qrNonce,
            long createdOfflineAt,
            long syncedAt
    ) {}

    public record SyncBatchResult(
            String posTerminalId,
            int syncedCount,
            int skippedDuplicates,
            List<String> rejectedReasons
    ) {}

    public record H3Zone(
            String zoneId,
            String h3Index,
            String name,
            String zoneType,
            int capacity,
            int currentCount,
            double bottleneckThreshold,
            String status,
            double occupancyRatio,
            long updatedAt
    ) {}

    public record FlashDiscount(
            String discountId,
            String congestedZoneId,
            String targetZoneId,
            int discountPercent,
            String promoMessage,
            boolean active,
            long createdAt
    ) {}
}
