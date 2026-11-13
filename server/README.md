# Server — three small services (Rust planned)

The server is not one app. It is three services that run independently
and talk HTTP to each other, so three sub-teams can build them in
parallel. Contracts: `docs/CONTRACTS.md` (§4–§6).

| Service | Folder | Job | Talks to |
|---|---|---|---|
| session-api | `session-api/` | Creates sessions, owns the session secret | Host app, verification (secret lookup) |
| verification | `verification/` | Checks token currency + RSSI on each submission | Student app, session-api, attendance-store |
| attendance-store | `attendance-store/` | Dedupes + stores records, serves the list | Verification, host app (list) |

Rules:

- A service never touches another service's storage. Verification never
  writes records directly; it calls the attendance store.
- Each service has its own README, its own tests, and runs alone with
  `cargo run` (stub the other services when testing — see
  `docs/TEAMS.md`).
- Run everything locally with `docker compose up` from the repo root
  (see `docker-compose.yml`). Each service gets its own port; see the
  compose file for the mapping.
- All three use the shared `rust/token-core/` for token work — never
  re-implement HMAC in a service.
