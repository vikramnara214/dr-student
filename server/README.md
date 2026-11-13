# Server — not built yet (Rust planned)

Verifies attendance submissions from the student app.

## What it should do

Accept `{sessionId, studentId, token, RSSI}` and:

- reject if the token is not for the current 30s window (±1 window for
  clock skew),
- reject if RSSI is below the room threshold (starting point −75 dBm,
  tunable per room),
- dedupe (one student = one record per session),
- store the attendance record.

## Plan

Rust server using the shared `rust/token-core/` for token verification, so
the token logic is identical on host, server, and laptop beacon. Firebase
was considered for a v1 shortcut — the Rust server is the robust route and
the one this repo is going down.
