import { useState } from "react";
import { QRCodeSVG } from "qrcode.react";

const STORAGE_KEY = "pulsevenue_pending_txs";
const POS_ID = "POS_NORTH_01";
const BAR_WALLET_ID = "w_bar_north";

export default function BartenderPos() {
    const [offline, setOffline] = useState(false);
    const [amount, setAmount] = useState("25.00");
    const [qrText, setQrText] = useState("");
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState("");


    const [pending, setPending] = useState(() => {
        try {
            const saved = JSON.parse(
                localStorage.getItem(STORAGE_KEY) || "[]"
            );

            return Array.isArray(saved)
                ? saved.filter(
                    (tx) =>
                        tx &&
                        typeof tx.idempotencyKey === "string" &&
                        typeof tx.debitWalletId === "string" &&
                        Number.isFinite(tx.amount) &&
                        tx.amount > 0
                )
                : [];
        } catch {
            return [];
        }
    });


    async function loadDemoToken() {
        if (offline || loading) return;

        setLoading(true);
        setMessage("");

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
                throw new Error("Could not generate a token.");
            }

            const token = await response.json();

            setQrText(JSON.stringify(token, null, 2));
            setMessage(
                "Token loaded. It expires after 60 seconds."
            );
        } catch (error) {
            setMessage(error.message || "Backend unavailable.");
        } finally {
            setLoading(false);
        }
    }

    async function recordTransaction(event) {
        event.preventDefault();
        if (loading) return;

        setMessage("");

        const normalized = amount.trim().replace(",", ".");

        if (!/^\d+(\.\d{1,2})?$/.test(normalized)) {
            setMessage(
                "Enter a valid amount with up to two decimals."
            );
            return;
        }

        const amountCents = Math.round(
            Number(normalized) * 100
        );

        if (
            !Number.isSafeInteger(amountCents) ||
            amountCents <= 0
        ) {
            setMessage("Enter a positive amount.");
            return;
        }

        const value = amountCents / 100;

        let token;

        try {
            token = JSON.parse(qrText);
        } catch {
            setMessage("QR token must be valid JSON.");
            return;
        }

        if (
            !token ||
            typeof token !== "object" ||
            Array.isArray(token) ||
            typeof token.walletId !== "string" ||
            !token.walletId ||
            typeof token.nonce !== "string" ||
            !token.nonce ||
            typeof token.signature !== "string" ||
            !token.signature ||
            token.currency !== "RON" ||
            !Number.isFinite(token.balanceSnapshot) ||
            token.balanceSnapshot < 0 ||
            !Number.isInteger(token.expiresAt) ||
            !Number.isInteger(token.issuedAt) ||
            token.issuedAt > token.expiresAt
        ) {
            setMessage("QR token has missing or invalid fields.");
            return;
        }

        const now = Math.floor(Date.now() / 1000);

        if (token.expiresAt <= now) {
            setMessage("Token expired. Generate a new one.");
            return;
        }


        const alreadyUsed = pending.some(
            (tx) =>
                tx.debitWalletId === token.walletId &&
                tx.tokenNonce === token.nonce
        );

        if (alreadyUsed) {
            setMessage(
                "This token already has a pending payment."
            );
            return;
        }

        const alreadySpentCents = pending
            .filter(
                (tx) => tx.debitWalletId === token.walletId
            )
            .reduce(
                (total, tx) =>
                    total + Math.round(tx.amount * 100),
                0
            );

        const balanceCents = Math.round(
            token.balanceSnapshot * 100
        );

        if (amountCents + alreadySpentCents > balanceCents) {
            setMessage(
                "Insufficient balance in the local snapshot."
            );
            return;
        }

        setLoading(true);

        try {
            if (!offline) {
                const response = await fetch(
                    "/api/v1/qr/verify-offline",
                    {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                        },
                        body: JSON.stringify({
                            ...token,
                            orderAmount: value,
                            localAlreadySpent: alreadySpentCents / 100,
                        }),
                    }
                );

                if (!response.ok) {
                    throw new Error(
                        "Token verification request failed."
                    );
                }

                const result = await response.json();

                if (result.valid !== true) {
                    throw new Error(
                        result.reason || "Token rejected."
                    );
                }
            }

            const transaction = {
                idempotencyKey: crypto.randomUUID(),
                debitWalletId: token.walletId,
                creditWalletId: BAR_WALLET_ID,
                amount: value,
                createdOfflineAt: now,
                tokenNonce: token.nonce,
                status: offline
                    ? "UNVERIFIED_OFFLINE"
                    : "PENDING_SYNC",
            };

            const updated = [...pending, transaction];

            localStorage.setItem(
                STORAGE_KEY,
                JSON.stringify(updated)
            );

            setPending(updated);
            setQrText("");

            setMessage(
                offline
                    ? "Transaction saved locally. Signature NOT verified offline."
                    : "Token verified by backend. Transaction pending sync."
            );
        } catch (error) {
            setMessage(
                error.message || "Could not record transaction."
            );
        } finally {
            setLoading(false);
        }
    }

    async function syncBatch() {
        if (offline || loading || pending.length === 0) return;

        setLoading(true);
        setMessage("");

        const transactions = pending.map((tx) => ({
            idempotencyKey: tx.idempotencyKey,
            debitWalletId: tx.debitWalletId,
            creditWalletId: tx.creditWalletId,
            amount: tx.amount,
            createdOfflineAt: tx.createdOfflineAt,
        }));

        try {
            const response = await fetch(
                "/api/v1/transactions/sync",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        posTerminalId: POS_ID,
                        transactions,
                    }),
                }
            );

            if (!response.ok) {
                throw new Error("Batch synchronization failed.");
            }

            const result = await response.json();

            if (
                !Number.isInteger(result.syncedCount) ||
                result.syncedCount < 0 ||
                !Number.isInteger(result.skippedDuplicates) ||
                result.skippedDuplicates < 0 ||
                !Array.isArray(result.rejectedReasons) ||
                result.rejectedReasons.length > 0 ||
                result.syncedCount + result.skippedDuplicates !==
                pending.length
            ) {
                throw new Error(
                    "Partial or unexpected sync result. Queue kept for review."
                );
            }

            localStorage.setItem(STORAGE_KEY, "[]");
            setPending([]);

            setMessage(
                `Sync complete: ${result.syncedCount} synced, ` +
                `${result.skippedDuplicates} duplicates skipped.`
            );
        } catch (error) {
            setMessage(
                error.message || "Sync failed. Pending queue kept."
            );
        } finally {
            setLoading(false);
        }
    }

    return (
        <section className="card pos-card">
            <div className="pos-header">
                <div>
                    <h2>Bartender Tablet POS</h2>
                    <p>PulseVenue</p>
                </div>

                <label className="pos-mode">
                    <input
                        type="checkbox"
                        checked={offline}
                        disabled={loading}
                        onChange={(event) =>
                            setOffline(event.target.checked)
                        }
                    />
                    {offline ? "Offline simulation" : "Online"}
                </label>
            </div>


            {pending.some(
                (tx) => tx.status === "UNVERIFIED_OFFLINE"
            ) && (
                    <p className="pos-warning" role="note">
                        Demo limitation: backend batch sync does not
                        reverify offline QR signatures.
                    </p>
                )}

            <div className="pos-layout">
                <form
                    className="pos-panel"
                    onSubmit={recordTransaction}
                >
                    <h3>New Transaction</h3>

                    <label htmlFor="pos-amount">
                        Order amount (RON)
                    </label>

                    <input
                        id="pos-amount"
                        type="text"
                        inputMode="decimal"
                        value={amount}
                        onChange={(event) =>
                            setAmount(event.target.value)
                        }
                        disabled={loading}
                        required
                    />


                    <div className="pos-token-section">
                        <label>Attendee QR Payment</label>

                        <div className="pos-qr-preview">
                            {qrText.trim() ? (
                                <>
                                    <div className="pos-qr-image">
                                        <QRCodeSVG
                                            value={qrText}
                                            size={180}
                                            level="M"
                                            marginSize={4}
                                            bgColor="#FFFFFF"
                                            fgColor="#101820"
                                            title="Payment token QR preview"
                                        />
                                    </div>

                                    <p className="pos-token-status">
                                        QR token loaded
                                    </p>

                                    <small>
                                        Token not yet verified
                                    </small>
                                </>
                            ) : (
                                <>

                                    <svg
                                        className="pos-scan-icon"
                                        width="64"
                                        height="64"
                                        viewBox="0 0 24 24"
                                        fill="none"
                                        stroke="currentColor"
                                        strokeWidth="1.8"
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        aria-hidden="true"
                                    >
                                        <path d="M4 8V6a2 2 0 0 1 2-2h2" />
                                        <path d="M16 4h2a2 2 0 0 1 2 2v2" />
                                        <path d="M20 16v2a2 2 0 0 1-2 2h-2" />
                                        <path d="M8 20H6a2 2 0 0 1-2-2v-2" />
                                        <path d="M9 12h6" />
                                    </svg>


                                    <p className="pos-token-status">
                                        No QR token loaded
                                    </p>

                                    <small>
                                        Load a demo token or enter one manually
                                    </small>
                                </>
                            )}
                        </div>

                        <button
                            type="button"
                            className="pos-secondary-button"
                            onClick={loadDemoToken}
                            disabled={offline || loading}
                        >
                            Load QR Token
                        </button>

                        {offline && (
                            <p className="pos-token-hint">
                                Use a token loaded before going offline,
                                or enter an existing token manually.
                            </p>
                        )}

                        <details className="pos-manual-input">
                            <summary>Manual token input (testing)</summary>

                            <label htmlFor="pos-token">
                                QR token data (JSON)
                            </label>

                            <textarea
                                id="pos-token"
                                rows={5}
                                value={qrText}
                                onChange={(event) => setQrText(event.target.value)}
                                placeholder="Paste QR token here"
                                disabled={loading}
                            />
                        </details>
                    </div>

                    <button
                        type="submit"
                        className="pos-primary-button"
                        disabled={loading}
                    >
                        {loading
                            ? "Processing..."
                            : "Record Transaction"}
                    </button>
                </form>


                <div className="pos-panel">
                    <h3>
                        Pending Transactions ({pending.length})
                    </h3>

                    {pending.length === 0 ? (
                        <p>No pending transactions.</p>
                    ) : (
                        <ul className="pos-transaction-list">
                            {pending.map((tx) => (
                                <li key={tx.idempotencyKey}>
                                    <strong>
                                        {tx.amount.toFixed(2)} RON
                                    </strong>
                                    <span>{tx.debitWalletId}</span>
                                    <small>{tx.status}</small>
                                </li>
                            ))}
                        </ul>
                    )}

                    {pending.length > 0 && (
                        <button
                            type="button"
                            className="pos-primary-button"
                            onClick={syncBatch}
                            disabled={offline || loading}
                        >
                            {loading
                                ? "Please wait..."
                                : "Sync Batch to Cloud Ledger"}
                        </button>
                    )}
                </div>
            </div>

            {message && (
                <p
                    className="pos-feedback"
                    role="status"
                    aria-live="polite"
                >
                    {message}
                </p>
            )}
        </section>
    );
}
