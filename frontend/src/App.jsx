import React, { useState } from "react";
import AttendeeWallet from "./components/AttendeeWallet";
import OrganizerDashboard from "./components/OrganizerDashboard";
import BartenderPos from "./components/BartenderPos";

/**
 * Barebone React Application Skeleton for PulseVenue.
 * Initialized by: Software Architect / DevOps (ISS-01, ISS-03).
 *
 * Frontend Developer Pull Request Tasks:
 * - TODO (ISS-06): Build AttendeeWallet component (Rotating Signed QR + Balance + Flash Promo Banner)
 * - TODO (ISS-13): Build BartenderPos component (Offline Mode toggle + local HMAC check + localStorage queue + Batch Sync)
 * - TODO (ISS-14): Build OrganizerDashboard component (Uber H3 Zone occupancy cards + Bottleneck alerts)
 * - TODO (ISS-22): Add visual QR SVG renderer & 80,000-Agent Simulator trigger button
 */
export default function App() {
  const [activeTab, setActiveTab] = useState("ATTENDEE");

  return (
    <div className="app-shell">
      <header className="app-header">
        <a href="/" className="app-logo" aria-label="PulseVenue — Home">
          <img
            src="/pulsevenue-logo-horizontal.svg"
            alt="PulseVenue"
          />
        </a>
        <nav className="tab-nav">
          <button
            className={activeTab === "ATTENDEE" ? "active" : ""}
            onClick={() => setActiveTab("ATTENDEE")}
          >
            1. Attendee Wallet (QR)
          </button>
          <button
            className={activeTab === "POS" ? "active" : ""}
            onClick={() => setActiveTab("POS")}
          >
            2. Bartender Tablet POS (Offline)
          </button>
          <button
            className={activeTab === "ORGANIZER" ? "active" : ""}
            onClick={() => setActiveTab("ORGANIZER")}
          >
            3. Organizer H3 Grid &amp; Simulator
          </button>
        </nav>
      </header>

      <main className="app-content">
        {activeTab === "ATTENDEE" && <AttendeeWallet />}
        {activeTab === "POS" && <BartenderPos />}

        {activeTab === "ORGANIZER" && <OrganizerDashboard />}
      </main>
    </div>
  );
}
