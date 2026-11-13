# Contracts — the frozen agreements between parts

This is the file that makes parallel work possible. Every team builds
against these contracts, not against each other's unfinished code. If you
are on the student team, you do not wait for the server team — you build
against the API written here, with a stub server if you have to.

**Rule: a contract changes only by a PR that updates this file, approved
by the maintainer, with a note to every team. Never change a format in
code without changing it here first.**

All contracts are v1. If a breaking change is ever needed, it becomes v2
and v1 keeps working until every part has moved.

## 1. Token (frozen — matches `TokenRotator.kt` today)

- Algorithm: `HMAC-SHA256`, key = session secret as UTF-8 bytes.
- Message: the window number as a decimal string, UTF-8 bytes.
  Window number = `floor(unixMillis / 30000)` — 30-second windows aligned
  to the Unix epoch.
- Token: first 4 bytes of the HMAC, rendered as 8 lowercase hex chars.
  Example: secret `...`, window `...` → token like `3fa9c21e`.
- Verification accepts the **current window and the previous one**
  (clock skew between phones).
- The Rust `token-core` MUST produce byte-identical tokens to the Kotlin
  `TokenRotator` for the same secret + window. There is a test for this;
  if it fails, the Rust side is wrong, not the spec.

## 2. BLE advertisement (frozen — matches `BleAdvertiser.kt` today)

- Service UUID: `b5a3f2c1-4d6e-4a8b-9c0d-1e2f3a4b5c6d`. Students filter
  scans on this UUID.
- Service data: the 8-character token as UTF-8 bytes (8 bytes).
- Non-connectable advertisement, low-latency mode, high TX power.
- Total payload is 31 bytes (the BLE ad limit): flags (3) + 128-bit
  service UUID (18) + token (10). Do not add fields to the ad — there
  is no room.
- The token in the ad is re-advertised whenever the 30s window rolls over.

## 3. QR payload (frozen — matches `MainActivity.kt` today)

- The QR contains exactly the session ID, nothing else.
- Session ID format: `ATT-` + 8 uppercase hex chars, e.g. `ATT-1A2B3C4D`.
- The student app treats any other QR content as "not our session" and
  shows an error instead of crashing.

## 4. Session creation (new in the distributed design)

The starter generates the session secret on the host phone, which means
the server cannot verify tokens — it never sees the secret. In the
distributed design the **session service owns the secret**:

```
Host app → POST /sessions → session-service
  ← { "sessionId": "ATT-1A2B3C4D", "secret": "<uuid>", ... }
Host app advertises tokens derived from that secret.
```

- The secret travels to the host **once, over HTTPS**, and is never shown
  on screen, never put in the QR, and never logged.
- The host team builds against a stub of this endpoint until the session
  service exists (see `docs/TEAMS.md` — stubs).
- Until the session service exists, the host app keeps its current
  local-secret behaviour so it stays testable with nRF Connect. The
  switch to server-issued secrets is one tracked issue, not a rewrite.

## 5. Attendance submission API (v1)

Student app → verification service. JSON over HTTPS.

```
POST /attendance
{
  "sessionId": "ATT-1A2B3C4D",
  "studentId": "22A91A0501",
  "token": "3fa9c21e",
  "rssi": -68
}
```

Response — accepted:

```json
{ "status": "present", "sessionId": "ATT-1A2B3C4D" }
```

Response — rejected (HTTP 200 with a reason, not an error — a rejection
is a normal outcome, not a failure):

```json
{ "status": "rejected", "reason": "stale_token | weak_signal | duplicate | unknown_session" }
```

Rules:

- `studentId` is whatever the college uses (roll number). The server does
  not invent identities.
- `rssi` is the signal strength the student phone measured, in dBm
  (negative number, e.g. `-68`).
- Rejection reasons are exactly the four strings above. The student app
  shows a friendly message per reason.
- The verification service never stores attendance itself — on accept it
  forwards the record to the attendance store (contract 6).

## 6. Between server services (v1)

Verification → attendance store, JSON over HTTP (same machine in v1,
separate services so they can scale and deploy independently later):

```
POST /records
{ "sessionId": "...", "studentId": "...", "rssi": -68, "verifiedAt": "2026-11-13T11:30:00Z" }
→ { "status": "recorded" | "duplicate" }
```

- Dedupe lives in the attendance store: one record per
  (sessionId, studentId). A second submission returns `duplicate` and
  does not create a second record.
- Reading the attendance list (`GET /sessions/{id}/attendance`) is the
  attendance store's job, not the verification service's.

## 7. Login — face recognition (FRS, v1)

Login is identity, and it happens **in the student app, before anything
else** (full design: `android/features/frs/README.md`).

- The student enters their `studentId`, then FRS verifies their face
  against the face enrolled on that phone.
- FRS is **on-device only**: embeddings are computed and stored on the
  phone, encrypted; no face photo or embedding ever leaves the phone.
  There is no server API for faces, and there must never be one.
- The student app must not submit attendance (§5) unless FRS login
  passed for this session. This gate runs in the app — the server
  cannot verify it in v1 and does not try to (see the FRS README's
  known limits).
- After 3 failed login attempts, FRS login locks and the student is
  sent to the host for a manual check. There is no self-serve bypass.

## 8. What is deliberately NOT a contract yet

- Server-side accounts / passwords. Login is FRS on the device; the
  server still trusts the `studentId` in the submission. Do not bolt
  on a password system mid-build.
- Liveness checking for FRS (blink / head-turn anti-spoof). Tracked
  follow-up, not v1 — see the FRS README.
- The host's live attendance list UI. It reads from the attendance
  store's `GET /sessions/{id}/attendance` when that exists; until then
  the host app shows no list.
