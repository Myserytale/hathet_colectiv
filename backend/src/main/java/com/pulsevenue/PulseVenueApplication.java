package com.pulsevenue;

import com.pulsevenue.model.Models.*;
import com.pulsevenue.service.PulseVenueService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PulseVenueApplication {

    private final PulseVenueService service;

    public PulseVenueApplication(PulseVenueService service) {
        this.service = service;
    }

    public void startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // GET /api/v1/health
        server.createContext("/api/v1/health", exchange -> {
            if (isMethod(exchange, "GET")) {
                String json = String.format("{\"status\":\"UP\",\"service\":\"pulsevenue-backend\",\"wallets\":%d}",
                        service.getWallets().size());
                sendJsonResponse(exchange, 200, json);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            }
        });

        // GET /api/v1/state
        server.createContext("/api/v1/state", exchange -> {
            if (isMethod(exchange, "GET")) {
                int walletCount = service.getWallets().size();
                int zoneCount = service.getZones().size();
                String json = String.format("{\"status\":\"ACTIVE\",\"walletsCount\":%d,\"zonesCount\":%d}",
                        walletCount, zoneCount);
                sendJsonResponse(exchange, 200, json);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            }
        });

        // POST /api/v1/qr/generate
        server.createContext("/api/v1/qr/generate", exchange -> {
            if (isMethod(exchange, "POST")) {
                String body = readRequestBody(exchange);
                String walletId = parseJsonString(body, "walletId", "w_attendee_01");
                int ttlSeconds = parseJsonInt(body, "ttlSeconds", 300);
                long now = Instant.now().getEpochSecond();

                QrToken token = service.generateQrToken(walletId, ttlSeconds, now);

                String jsonResponse = String.format(
                        "{\"walletId\":\"%s\",\"ownerName\":\"%s\",\"balanceSnapshot\":%.2f,\"currency\":\"%s\",\"nonce\":\"%s\",\"issuedAt\":%d,\"expiresAt\":%d,\"signature\":\"%s\"}",
                        token.walletId(), token.ownerName(), token.balanceSnapshot(),
                        token.currency(), token.nonce(), token.issuedAt(), token.expiresAt(), token.signature()
                );
                sendJsonResponse(exchange, 200, jsonResponse);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            }
        });

        // POST /api/v1/qr/verify-offline
        server.createContext("/api/v1/qr/verify-offline", exchange -> {
            if (isMethod(exchange, "POST")) {
                String body = readRequestBody(exchange);

                double orderAmount = parseJsonDouble(body, "orderAmount", 0.0);
                double localAlreadySpent = parseJsonDouble(body, "localAlreadySpent", 0.0);
                long now = Instant.now().getEpochSecond();

                // Reconstruct incoming QrToken fully from request payload
                QrToken token = new QrToken(
                        parseJsonString(body, "walletId", ""),
                        parseJsonString(body, "ownerName", "Attendee"),
                        parseJsonDouble(body, "balanceSnapshot", 0.0),
                        parseJsonString(body, "currency", "RON"),
                        parseJsonString(body, "nonce", ""),
                        parseJsonLong(body, "issuedAt", now),
                        parseJsonLong(body, "expiresAt", now),
                        parseJsonString(body, "signature", "")
                );

                OfflineVerificationResult result = service.verifyQrTokenOffline(
                        token, orderAmount, localAlreadySpent, now
                );

                String jsonResponse = String.format(
                        "{\"valid\":%b,\"reason\":\"%s\",\"remainingOfflineBalance\":%.2f}",
                        result.valid(), result.reason(), result.remainingOfflineBalance()
                );
                sendJsonResponse(exchange, 200, jsonResponse);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            }
        });

        // POST /api/v1/transactions/sync
        server.createContext("/api/v1/transactions/sync", exchange -> {
            if (isMethod(exchange, "POST")) {
                String body = readRequestBody(exchange);
                String posId = parseJsonString(body, "posTerminalId", "POS_NORTH_01");
                long now = Instant.now().getEpochSecond();

                List<OfflineTransaction> transactions = parseOfflineTransactions(body);
                SyncBatchResult result = service.syncOfflineBatch(posId, transactions, now);

                String jsonResponse = String.format(
                        "{\"posTerminalId\":\"%s\",\"syncedCount\":%d,\"skippedDuplicates\":%d,\"rejectedReasons\":%s}",
                        result.posTerminalId(),
                        result.syncedCount(),
                        result.skippedDuplicates(),
                        toJsonArray(result.rejectedReasons())
                );
                sendJsonResponse(exchange, 200, jsonResponse);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            }
        });

        // POST /api/v1/telemetry/simulate-tick
        server.createContext("/api/v1/telemetry/simulate-tick", exchange -> {
            if (isMethod(exchange, "POST")) {
                String body = readRequestBody(exchange);
                long now = Instant.now().getEpochSecond();

                Map<String, Integer> zoneCounts = parseZoneTelemetryCounts(body);
                List<FlashDiscount> discounts = service.updateZoneTelemetry(zoneCounts, now);

                String discountsJson = discounts.stream().map(d -> String.format(
                        "{\"discountId\":\"%s\",\"congestedZoneId\":\"%s\",\"targetZoneId\":\"%s\",\"discountPercent\":%d,\"promoMessage\":\"%s\",\"active\":%b,\"createdAt\":%d}",
                        d.discountId(), d.congestedZoneId(), d.targetZoneId(), d.discountPercent(),
                        d.promoMessage(), d.active(), d.createdAt()
                )).collect(Collectors.joining(",", "[", "]"));

                String jsonResponse = String.format(
                        "{\"status\":\"processed\",\"activeDiscountsCount\":%d,\"discounts\":%s}",
                        discounts.size(), discountsJson
                );
                sendJsonResponse(exchange, 200, jsonResponse);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("PulseVenue Backend active on http://localhost:" + port);
    }

    // --- HELPER METHODS ---

    private boolean isMethod(HttpExchange exchange, String method) {
        return method.equalsIgnoreCase(exchange.getRequestMethod());
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String parseJsonString(String json, String key, String defaultValue) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : defaultValue;
    }

    private int parseJsonInt(String json, String key, int defaultValue) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : defaultValue;
    }

    private long parseJsonLong(String json, String key, long defaultValue) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : defaultValue;
    }

    private double parseJsonDouble(String json, String key, double defaultValue) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*([0-9.]+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : defaultValue;
    }

    private String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        return list.stream()
                .map(item -> "\"" + item.replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private List<OfflineTransaction> parseOfflineTransactions(String json) {
        List<OfflineTransaction> list = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{[^{}]*\"idempotencyKey\"[^{}]*\\}");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String txBlock = matcher.group();
            String key = parseJsonString(txBlock, "idempotencyKey", "");
            String debit = parseJsonString(txBlock, "debitWalletId", "");
            String credit = parseJsonString(txBlock, "creditWalletId", "");
            double amount = parseJsonDouble(txBlock, "amount", 0.0);
            long created = parseJsonLong(txBlock, "createdOfflineAt", Instant.now().getEpochSecond());

            list.add(new OfflineTransaction(key, debit, credit, amount, null, created));
        }
        return list;
    }

    private Map<String, Integer> parseZoneTelemetryCounts(String json) {
        Map<String, Integer> counts = new HashMap<>();
        Pattern pattern = Pattern.compile("\"(8a2a[0-9a-f]+)\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            counts.put(matcher.group(1), Integer.parseInt(matcher.group(2)));
        }
        return counts;
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        new PulseVenueApplication(new PulseVenueService()).startServer(port);
    }
}