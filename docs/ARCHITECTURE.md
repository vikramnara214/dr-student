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

## Parallel layout — feature systems, plugged together

The project is organised by **feature system**, so teams can build in
parallel. Each system is an independent module with a public interface
and its own tests; the two apps are thin shells that plug the systems
together. See [TEAMS.md](TEAMS.md) for who owns what.

```
  Host app (shell)                     Student app (shell)
  android/host-app/                    android/student-app/
  • session screen                     • scan + submit screens
  • wires systems together             • wires systems together
        │                                      │
        ▼                                      ▼
  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
  │ QR system   │  │ BLE system  │  │ Permissions │  │ FRS system  │
  │ features/qr │  │ features/   │  │ features/   │  │ features/   │
  │ generate +  │  │ ble: adv. + │  │ permissions │  │ frs: face   │
  │ scan        │  │ scan + RSSI │  │ all runtime │  │ recognition │
  └─────────────┘  └─────────────┘  │ permission  │  │ at login,   │
                                    │ flows       │  │ on-device   │
                                    └─────────────┘  └─────────────┘
  ┌─────────────┐                   └─────────────┘
  │ Token system│  rust/token-core (+ Kotlin TokenRotator) — one HMAC
  │             │  implementation, used by host, server, laptop beacon
  └─────────────┘
        │
        ▼
  Server (3 services, HTTP) — session-api · verification · attendance-store
  Dev tool: laptop-beacon (Rust + bluer) — a laptop advertising the token
  so the student app can be tested without a second phone.
```

Rules of the layout:

- A system is used only through its public interface. Nobody reaches
  into another system's internals.
- A system builds and tests alone, without the apps.
- No feature asks for permissions directly — every request goes through
  the permissions system.
- On the server side, a service never reaches into another service's
  storage. Verification never writes attendance records directly; it
  calls the attendance store. The store never verifies tokens.
- The session secret lives in exactly one place: the session service
  (and, transiently, the host phone that asked for it). Nothing else
  ever sees it.
- Every boundary above is a contract in `CONTRACTS.md`. If two parts
  disagree, the contract wins and the code is wrong.

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
| Host app | Session QR shows, token advertises + rotates every 30s, verified in nRF Connect; later: secret comes from the session service |
| Student app | Scans QR, hears token, submits; handles "no token heard" gracefully |
| QR system | Generates a scannable QR from a session ID; scans a QR and returns the session ID (or a clean error) |
| BLE system | Advertises the token (host) and scans + reports token + RSSI (student), per contract §2 |
| Permissions system | One flow for asking, explaining, and handling denied permissions, used by both apps |
| FRS system | Face recognition at login: enrol once, verify each login, on-device only, no face data leaves the phone |
| Token core (Rust) | Same token as the Kotlin `TokenRotator` for the same secret + window (test this) |
| Session service | `POST /sessions` returns a sessionId + secret; secret retrievable for verification |
| Verification service | Accepts a valid submission, rejects stale token / weak RSSI / unknown session with the exact reason strings |
| Attendance store | One record per (session, student); duplicate returns `duplicate`; serves the attendance list |
| Laptop beacon | Laptop advertises the token via its own Bluetooth so the student app can be tested without a second phone |
