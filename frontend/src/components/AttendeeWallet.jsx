import { useEffect, useState } from "react";
import { QRCodeSVG } from "qrcode.react";

export default function AttendeeWallet() {
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [secondsLeft, setSecondsLeft] = useState(0);

  useEffect(() => {
    if (!token) return;

    function updateCountdown() {
      const remaining = token.expiresAt - Math.floor(Date.now() / 1000);
      setSecondsLeft(Math.max(0, remaining));
    }

    updateCountdown();

    const interval = setInterval(updateCountdown, 1000);

    return () => clearInterval(interval);
  }, [token]);

  async function generateToken() {
    setLoading(true);
    setError("");
    setToken(null);

    try {
      const response = await fetch("/api/v1/qr/generate", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          walletId: "w_attendee_01",
          ttlSeconds: 60,
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to generate payment token.");
      }

      const data = await response.json();
      setToken(data);
    } catch (err) {
      setError(err.message || "Connection error.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="card wallet-card">
      <h2>Attendee Wallet</h2>

      <p>
        Generate a secure payment token for purchases at PulseVenue.
      </p>

      {token && (
        <div>
          <h3>{token.ownerName}</h3>

          <p>Available balance</p>

          <p className="type-kpi">
            {token.balanceSnapshot.toFixed(2)} {token.currency}
          </p>

          <h3>Payment Token</h3>

          <p>
            {secondsLeft > 0
              ? `Expires in ${secondsLeft} seconds`
              : "Token expired"}
          </p>

          {secondsLeft > 0 ? (
            <div
                style={{
                display: "inline-flex",
                padding: "16px",
                backgroundColor: "#FFFFFF",
                borderRadius: "12px",
                maxWidth: "100%",
                }}
            >
                <QRCodeSVG
                value={JSON.stringify(token)}
                size={280}
                level="M"
                marginSize={4}
                bgColor="#FFFFFF"
                fgColor="#101820"
                title="PulseVenue payment QR code"
                style={{
                    width: "100%",
                    height: "auto",
                    maxWidth: "280px",
                }}
                />
            </div>
            ) : (
            <p role="status">
                QR code expired. Generate a new payment token.
            </p>
            )}
        </div>
      )}

      {error && <p role="alert">{error}</p>}

      <button
        type="button"
        className="wallet-generate-button"
        onClick={generateToken}
        disabled={loading}
    >
        {loading ? "Generating..." : "Generate Payment Token"}
      </button>
    </section>
  );
}