# attendance-store — not built yet (Rust)

Owns attendance records. The only service that stores them.

## API (contract §6)

```
POST /records
{ "sessionId": "...", "studentId": "...", "rssi": -68,
  "verifiedAt": "2026-11-13T11:30:00Z" }
→ { "status": "recorded" | "duplicate" }

GET /sessions/{sessionId}/attendance
→ { "sessionId": "...",
    "records": [ { "studentId": "...", "rssi": -68,
                   "verifiedAt": "..." } ] }
```

Rules:

- Dedupe: one record per (sessionId, studentId). A repeat returns
  `duplicate` and stores nothing new.
- Storage for v1 is the team's choice (SQLite is the sane default —
  one file, no server to run). Propose it in an issue before building;
  the choice must be boring and local-first.
- This service never verifies tokens and never sees session secrets.
  It trusts that the verification service already checked.

## Done when

- Records a submission, returns `duplicate` on repeat, and serves the
  list for a session — all via `curl`.
