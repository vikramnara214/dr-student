# session-api — not built yet (Rust)

Owns sessions and their secrets. The only service that ever holds a
session secret (besides the host phone it was issued to).

## API (contract §4)

```
POST /sessions
→ { "sessionId": "ATT-1A2B3C4D", "secret": "<uuid>" }
```

- Generates the session ID (`ATT-` + 8 uppercase hex chars) and a random
  secret (UUID).
- Stores the secret so the verification service can look it up by
  session ID. (How services share this in v1 — same database, or an
  internal lookup endpoint — is a decision for the server team to
  propose in an issue before building. Do not guess it in code.)
- The secret is returned once, over HTTPS, to the host that created the
  session. It is never logged and never appears in any other response.

## Done when

- `POST /sessions` works via `curl`, returns a fresh sessionId + secret
  each time, and the secret is retrievable for verification.
