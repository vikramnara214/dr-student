# Good first issues — starter list for the maintainer

Create these as GitHub issues (label: `good first issue`) once the repo is
live. Each one is small, self-contained, and learnable in an evening.
Assign one person per issue.

## Tiny / docs (first PR ever)

1. **Docs: add screenshots to the host-app README** — run the host app,
   screenshot the session screen, add them to `android/host-app/README.md`.
2. **Docs: fix or expand SETUP.md for Windows/macOS** — SETUP.md is written
   from a Pop!_OS laptop; note anything different on other OSes.
3. **Add a `.gitignore` check** — verify Android build outputs / local
   files are ignored and nothing personal is tracked.

## Host app (Kotlin, small)

4. **Show the token countdown clearly** — the token rotates every 30s;
   make the countdown obvious in the UI and handle the rotation edge
   (token changing mid-display).
5. **Handle Bluetooth-off gracefully** — if Bluetooth is off or permission
   is denied, show a clear message + a button to open settings instead
   of crashing or silently doing nothing.
6. **Stop session cleanly** — confirm Stop actually stops advertising and
   clears the session state; add a visible "session stopped" state.

## Student app (the big build, split small)

7. **Student app skeleton** — new Android module that opens, asks for
   camera + Bluetooth permissions, and shows a placeholder screen.
   (Just the skeleton — no scanning yet.)
8. **QR scan screen** — scan the session QR with CameraX + ZXing / ML Kit
   and display the session ID that was scanned.
9. **BLE scan + display token** — scan for the service UUID, display the
   heard token + RSSI. No submitting yet — just prove the phone hears it.

## Shared / Rust (later, for the keener juniors)

10. **Rust token-core: HMAC + 30s windows** — implement the same token
    scheme as `TokenRotator.kt` in Rust, with a test proving both produce
    the same token for the same secret + window.
11. **Laptop beacon (Rust + `bluer`)** — advertise the token from a Linux
    laptop so the student app can be tested without a second phone.

## Rules for these issues

- One assignee each. If someone goes quiet for a week, unassign and reopen.
- Every issue should state: what "done" looks like, which files to look
  at, and how to test it. If an issue does not say that, it is not ready
  to assign — fix the issue text first.
