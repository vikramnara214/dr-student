# Student app — not built yet

This is the second app in the project, and the main build for contributors.

## What it should do

1. Scan the session QR (CameraX + ZXing / ML Kit) → session ID.
2. Listen for the BLE beacon (service UUID
   `b5a3f2c1-4d6e-4a8b-9c0d-1e2f3a4b5c6d`) → current token + RSSI.
3. Submit `{sessionId, studentId, token, RSSI}` to the server.
4. Show clearly: present / token not heard / token expired / no network.

## Build it in small steps (see `docs/GOOD_FIRST_ISSUES.md`)

1. Skeleton module (permissions + placeholder screen).
2. QR scan screen only.
3. BLE scan + display token/RSSI only.
4. Submit to server (once `server/` exists; until then, log the payload).

Do not build all four in one PR — one step per PR, so it can actually be
reviewed.
