# verification — not built yet (Rust)

Checks each attendance submission. Stateless apart from the session
secret lookup — it stores nothing itself.

## API (contract §5)

```
POST /attendance
{ "sessionId": "ATT-1A2B3C4D", "studentId": "22A91A0501",
  "token": "3fa9c21e", "rssi": -68 }
→ { "status": "present", "sessionId": "ATT-1A2B3C4D" }
→ { "status": "rejected",
    "reason": "stale_token | weak_signal | duplicate | unknown_session" }
```

Steps per submission, in order:

1. Look up the session secret (unknown session → reject `unknown_session`).
2. Verify the token with `rust/token-core` (current window ±1, else
   `stale_token`).
3. Check RSSI against the room threshold (starting point −75 dBm,
   tunable — else `weak_signal`).
4. Forward accepted records to the attendance store (contract §6). If
   the store says `duplicate`, the student gets `duplicate`.

## Done when

- Accepts a valid submission via `curl`, rejects each bad case with the
  exact reason string, and never writes attendance records itself.
