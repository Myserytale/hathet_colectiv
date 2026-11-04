package com.pulsevenue;

import com.pulsevenue.service.PulseVenueService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Barebone Java 21 Backend Entrypoint for PulseVenue.
 * Initialized by: Software Architect / DevOps (ISS-01, ISS-03).
 *
 * TODO (ISS-11 - Backend Developer 1):
 * Add REST endpoints / controllers for:
 * - GET  /api/v1/state
 * - POST /api/v1/qr/generate
 * - POST /api/v1/qr/verify-offline
 * - POST /api/v1/transactions/sync
 * - POST /api/v1/telemetry/simulate-tick
 */
public class PulseVenueApplication {

    private final PulseVenueService service;

    public PulseVenueApplication(PulseVenueService service) {
        this.service = service;
    }

    public void startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/v1/health", exchange -> {
            String json = "{\"status\":\"UP\",\"service\":\"pulsevenue-backend-skeleton\",\"wallets\":"
                    + service.getWallets().size() + "}";
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.setExecutor(null);
        server.start();
        System.out.println("PulseVenue Backend Skeleton running on http://localhost:" + port + "/api/v1/health");
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        new PulseVenueApplication(new PulseVenueService()).startServer(port);
    }
}
