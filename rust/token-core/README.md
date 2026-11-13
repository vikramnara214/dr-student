# token-core (Rust) — not built yet

Shared token logic, implemented once in Rust and used everywhere:

- `server/` — verification,
- the laptop beacon (Rust, `bluer` crate) — advertising from a laptop,
- Android apps via NDK — so Kotlin and Rust never drift apart.

## Spec (must match `TokenRotator.kt` exactly)

- Token = `HMAC-SHA256(secret, 30s time window)`, first 4 bytes as 8 hex chars.
- Windows are 30 seconds, aligned to Unix epoch.
- Verification accepts the current window ±1 window.

## Done when

A test proves the Rust implementation produces the same token as the Kotlin
`TokenRotator` for the same secret + window.
