# Architecture

## The problem

QR-code attendance is easy to fake: screenshot the QR, send it to a friend
outside the room, they scan it and get marked present. The QR proves "I saw
the code", not "I am in the room".

## The fix: QR + BLE, both required

Presence = scanned the session QR **and** heard the current BLE token nearby.

```
Host (1 phone)                    Students (50–70 phones)          Server
─────────────────                 ───────────────────────          ──────
Start session
  secret = random
Show QR(sessionId)
Advertise token ───── BLE ──────► Scan QR
  token = HMAC-SHA256             Hear token + RSSI
  (secret, 30s window)            Submit {sessionId,
  rotates every 30s                 studentId, token, RSSI} ─────► Verify:
                                                                    - token is
                                                                      current
                                                                      (±1 window)
                                                                    - RSSI ≥
                                                                      threshold
                                                                      (~−75 dBm,
                                                                      tune/room)
                                                                    - dedupe
                                                                    → present
```

## Why this shape

- **One advertiser, many scanners.** If 70 phones all tried to connect over
  BLE, the room would congest. Advertising is broadcast: the host just shouts
  the token, everyone listens.
- **Rotating token, 30s windows.** A token heard 5 minutes ago is useless —
  the server only accepts the current window (plus one window of clock skew).
- **RSSI as a rough distance check.** Signal strength falls with distance, so
  a −75 dBm threshold is a starting point for "in the room". It is rough and
  must be calibrated per room (walls, bodies, phone models all change it).
  Walk the room with nRF Connect and note RSSI at the back row.
- **Token in the clear, by design.** The token is not a secret — it only
  proves proximity in the last 30 seconds. The *session secret* never leaves
  the host / server.

## Known limits (documented, not bugs)

- A live in-room relay defeats it: someone in the room could re-broadcast
  the token to someone outside. This design stops screenshot forwarding,
  not a determined relay. Tighter options (e.g. challenge-response) are
  future work, not v1.
- RSSI is noisy. Thresholds need per-room tuning; a single fixed number
  will be wrong somewhere.
- BLE advertising needs a physical phone. Emulators cannot advertise.

## The Rust plan

To keep the token logic identical everywhere, the plan is a shared Rust core:

- `rust/token-core/` — HMAC token generation + verification, 30s windows.
  One implementation, used by:
  - `server/` (Rust server) — verification,
  - the laptop beacon (Rust, `bluer` crate) — for developing/testing from
    a laptop without a host phone,
  - Android apps via NDK — so Kotlin and Rust never drift apart.
- This is **not** a rewrite of the Android UI in Rust. Android stays Kotlin;
  Rust is the shared core + server + laptop beacon.

## Modules and done-when

| Module | Done when |
|---|---|
| Host app | Session QR shows, token advertises + rotates every 30s, verified in nRF Connect |
| Student app | Scans QR, hears token, submits; handles "no token heard" gracefully |
| Token core (Rust) | Same token as the Kotlin `TokenRotator` for the same secret + window (test this) |
| Server | Accepts a valid submission, rejects stale token / weak RSSI / duplicate |
| Laptop beacon | Laptop advertises the token via its own Bluetooth so the student app can be tested without a second phone |
