# BLE Attendance — Host Advertiser (starter)

Minimal host app for the QR + BLE attendance idea: this phone advertises a
rotating BLE token and shows a QR with the session ID. A student phone (to be
built next) scans the QR, hears the token, and submits both to the server.

This is step 1 only — the "start with just the host advertiser so you can test
the beacon with a BLE scanner app first" step. No student app, no server yet.

## What it does

- **Start session** → generates a random session secret, shows the session ID
  as a QR code, and starts BLE advertising a token.
- The token is `HMAC-SHA256(secret, 30s time window)`, first 4 bytes as 8 hex
  chars — same scheme as the design note. It rotates every 30 seconds; the
  countdown on screen shows when.
- **Stop** → stops advertising.

## Open it

1. Android Studio → Open → this folder. Let Gradle sync (needs internet once
   for the ZXing dependency).
2. Run on a **physical Android phone** (emulator can't advertise BLE).
3. Grant Bluetooth + Location permissions when asked — BLE advertising needs
   `BLUETOOTH_ADVERTISE`, and Android requires location permission for BLE.

## Test it (no student app needed)

1. On a second phone, install **nRF Connect** (free, Nordic Semiconductor).
2. In nRF Connect → Scanner, look for the service UUID
   `b5a3f2c1-4d6e-4a8b-9c0d-1e2f3a4b5c6d`.
3. The service data should show an 8-character hex token — and it should
   change every 30 seconds. If the token changes, the rotating beacon works.
4. Walk to the back of the room and note the RSSI in nRF Connect — that is
   your starting calibration point for the −75 dBm threshold (tune per room).

## Files

- `app/src/main/java/com/nara/attendance/host/MainActivity.kt` — UI + session
- `TokenRotator.kt` — HMAC token, 30 s windows
- `BleAdvertiser.kt` — start/stop BLE advertising wrapper
- `QrCode.kt` — session-ID → QR bitmap (ZXing)
- `AndroidManifest.xml` — permissions (Android 12+ BLE set)

## What's deliberately not here yet

- Student app (QR scan + BLE scan + submit) — step 2.
- Server verification (token currency + RSSI threshold + dedupe) — step 3.
- The token here is advertised in the clear by design: it only proves
  "heard nearby in the last 30 s". It stops screenshot/QR-forwarding, not a
  live in-room relay — same limit as the design note.

Built from the design delivered in chat on Nov 7, 2026. Not compiled here —
sync and run in Android Studio; fix whatever the compiler complains about
first, it will be a manifest/dependency nit, not the BLE logic.
