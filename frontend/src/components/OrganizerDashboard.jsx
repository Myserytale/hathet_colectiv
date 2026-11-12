import { useState } from "react";

const initialZones = [
  {
    id: "8a2a1072b59ffff",
    name: "Main Stage Arena",
    capacity: 45000,
    count: 28500,
  },
  {
    id: "8a2a1072b51ffff",
    name: "North Main Bar",
    capacity: 15000,
    count: 11800,
  },
  {
    id: "8a2a1072b12ffff",
    name: "Alchemy Bar",
    capacity: 18000,
    count: 4400,
  },
];

export default function OrganizerDashboard() {
  const [zones, setZones] = useState(initialZones);
  const [discounts, setDiscounts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [simulated, setSimulated] = useState(false);
  const [northBarCount, setNorthBarCount] = useState(11800);

  function getOccupancy(zone) {
    return (zone.count / zone.capacity) * 100;
  }

  function getStatus(occupancy) {
    if (occupancy >= 85) return "BOTTLENECK";
    if (occupancy >= 70) return "ELEVATED";
    return "NORMAL";
  }

  async function simulateCongestion() {
    setLoading(true);
    setError("");

    const updatedZones = [
        { ...initialZones[0], count: 28500 },
        { ...initialZones[1], count: northBarCount },
        { ...initialZones[2], count: 4400 },
    ];

    const zoneCounts = Object.fromEntries(
      updatedZones.map((zone) => [zone.id, zone.count])
    );

    try {
      const response = await fetch("/api/v1/telemetry/simulate-tick", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ zoneCounts }),
      });

      if (!response.ok) {
        throw new Error("Failed to simulate crowd congestion.");
      }

      const data = await response.json();

      if (data.status !== "processed") {
        throw new Error("Unexpected response from backend.");
      }

      setZones(updatedZones);
      setDiscounts(data.discounts || []);
      setSimulated(true);
    } catch (err) {
      setError(err.message || "Connection error.");
    } finally {
      setLoading(false);
    }
  }

    async function resetSimulation() {
        setLoading(true);
        setError("");

        try {
            const zoneCounts = Object.fromEntries(
            initialZones.map((zone) => [zone.id, zone.count])
            );

            const response = await fetch("/api/v1/telemetry/simulate-tick", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ zoneCounts }),
            });

            if (!response.ok) {
            throw new Error("Failed to reset simulation.");
            }

            const data = await response.json();

            if (data.status !== "processed" || data.activeDiscountsCount !== 0) {
            throw new Error("Simulation reset was not confirmed.");
            }

            setZones(initialZones);
            setNorthBarCount(11800);
            setDiscounts([]);
            setSimulated(false);
        } catch (err) {
            setError(err.message || "Connection error.");
        } finally {
            setLoading(false);
        }
    }

  return (
    <section className="card organizer-dashboard">
      <h2>Organizer Dashboard</h2>

      <p>
        Monitor crowd occupancy and detect congestion across the venue.
      </p>

      <h3>Crowd Monitoring</h3>

      <div className="organizer-grid">
        {zones.map((zone) => {
          const occupancy = getOccupancy(zone);
          const status = getStatus(occupancy);

          return (
            <div
            className={`organizer-zone organizer-zone--${status.toLowerCase()}`}
            key={zone.id}
            >
            <h3>{zone.name}</h3>

              <p>
                {zone.count.toLocaleString("en-US")} /{" "}
                {zone.capacity.toLocaleString("en-US")} attendees
              </p>

              <p className="type-kpi">
                {occupancy.toFixed(1)}%
              </p>

              <p>
                Status: <strong>{status}</strong>
              </p>

              <progress
                value={zone.count}
                max={zone.capacity}
                aria-label={`${zone.name} occupancy`}
              />
            </div>
          );
        })}
      </div>

      <div className="organizer-simulation-controls">
        <label htmlFor="north-bar-count">
            North Main Bar — Simulated Attendance
        </label>

        <p>
            <strong>{northBarCount.toLocaleString("en-US")}</strong> / 15,000
            attendees ({((northBarCount / 15000) * 100).toFixed(1)}%)
        </p>

        <input
            id="north-bar-count"
            type="range"
            min="0"
            max="15000"
            step="100"
            value={northBarCount}
            onChange={(event) => setNorthBarCount(Number(event.target.value))}
            disabled={loading}
        />

        <p>
            Congestion threshold: 85%. Click Simulate to process the
            selected attendance.
        </p>
       </div>
      <button
        type="button"
        className="wallet-generate-button"
        onClick={simulateCongestion}
        disabled={loading}
      >
        {loading ? "Simulating..." : "Simulate Crowd Congestion"}
      </button>

      <button
        type="button"
        className="organizer-reset-button"
        onClick={resetSimulation}
        disabled={loading}
        >
        Reset Simulation
      </button>

      {error && <p role="alert">{error}</p>}

      {simulated && (
        <div role="status">
          <h3>Simulation Results</h3>

          {discounts.length > 0 ? (
            discounts.map((discount) => (
              <div key={discount.discountId}>
                <h3>Flash Discount: {discount.discountPercent}% OFF</h3>
                <p>{discount.promoMessage}</p>
              </div>
            ))
          ) : (
            <p>No flash discounts generated.</p>
          )}
        </div>
      )}
    </section>
  );
}