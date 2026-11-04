import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Configured by Software Architect / DevOps (ISS-01, ISS-03)
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
