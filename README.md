# Dr Student

QR + BLE attendance — verify 50–70 phones are actually **in the room** for
attendance, not just that someone saw a QR code.

A QR alone can be screenshotted and forwarded to a friend outside the room.
So this app pairs the QR with a Bluetooth Low Energy (BLE) beacon: the host
phone broadcasts a token that rotates every 30 seconds, and a student phone
only counts as present if it **scanned the QR _and_ heard the current token
nearby**.

Built by students, for students — and built in the open so juniors can learn
how a real app is put together. Contributions welcome: see
[CONTRIBUTING.md](CONTRIBUTING.md).

## How it works

```
Host phone (teacher / mentor)              Student phones (50–70 in the room)
┌────────────────────────────┐             ┌────────────────────────────┐
│ 1. Start session           │             │ 1. Scan session QR       │
│    → random session secret │             │ 2. Listen for BLE beacon │
│ 2. Show session QR         │  QR + BLE   │ 3. Submit {sessionId,    │
│ 3. Advertise token:        │ ──────────► │    studentId, token,     │
│    HMAC(secret, 30s window)│             │    RSSI} to server       │
└────────────────────────────┘             └──────────────┬─────────────┘
                                                          │ server checks
                                                          ▼
                                               Token still current?
                                               RSSI strong enough
                                               (in-room, ~−75 dBm)?
                                               → present ✓
```

- Token = `HMAC-SHA256(secret, 30s time window)`, first 4 bytes as 8 hex chars.
  It proves "heard nearby in the last 30 seconds".
- This stops screenshot / QR-forwarding proxy attendance. It does **not** stop
  a live in-room relay (someone in the room re-broadcasting the token) — that
  is a known, documented limit, not a bug.
- One advertiser, many scanners: BLE advertising avoids the congestion of
  70 phones all trying to connect.

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the full design.

## Project layout

| Folder | What it is | Status |
|---|---|---|
| `android/host-app/` | Host app shell: session screen, wires the systems together | Starter ready — needs testing on a real phone |
| `android/student-app/` | Student app shell: scan + submit screens | Not built yet — good place to contribute |
| `android/features/qr/` | QR system: generate (host) + scan (student) | Generator exists in host app, moves here |
| `android/features/ble/` | Bluetooth system: advertise + scan + RSSI | Advertiser exists in host app, moves here |
| `android/features/permissions/` | Permissions system: all runtime permission flows | Not built yet — extracted from host app |
| `android/features/frs/` | FRS: face recognition at login, on-device only | Not built yet — see its README for the privacy rules |
| `server/session-api/` | Creates sessions, owns the session secret | Not built yet — Rust planned |
| `server/verification/` | Verifies token currency + RSSI | Not built yet — Rust planned |
| `server/attendance-store/` | Stores records, dedupes, serves the list | Not built yet — Rust planned |
| `rust/token-core/` | Shared token logic (HMAC, 30s windows) in Rust, used by server / beacon / Android via NDK | Not built yet |
| `docs/` | Architecture, contracts, teams, setup | New? Start with `docs/TEAMS.md`, then `docs/CONTRACTS.md` |

## Quick start

You need Android Studio and a **physical Android phone** to test the beacon —
emulators cannot advertise BLE. Full steps, including the emulator + laptop
setup for developing on a laptop, are in [docs/SETUP.md](docs/SETUP.md).

```bash
git clone https://github.com/vikramnara214/dr-student.git
cd dr-student
# Android Studio → Open → android/host-app → Run on a physical phone
# On a second phone, open nRF Connect and watch the token change every 30s.
```

## Contributing

Juniors / first-time contributors: start with issues labelled
[`good first issue`](https://github.com/vikramnara214/dr-student/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22).
Pick one, comment "I'd like to take this", wait to be assigned, then open a PR.
Full flow in [CONTRIBUTING.md](CONTRIBUTING.md) — including how reviews work:
the maintainer reviews every PR before it merges.

## Status

Early, honest status: the host beacon starter exists and is uncompiled — the
first job is to get it running and prove the token rotates. Student app and
server are next. See the issue tracker for what is up for grabs right now.
