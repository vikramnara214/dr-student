# laptop-beacon — not built yet (Rust + `bluer`)

A dev tool, not part of the product: a laptop advertising the BLE token
via its own Bluetooth, so the student app can be tested without a
second phone.

- Uses the shared `rust/token-core/` for the token — same token the
  host phone would advertise for the same secret + window.
- Advertises the same service UUID and service-data format as the host
  app (contract §2), so the student app cannot tell the difference.
- Linux-first (Pop!_OS); the `bluer` crate talks to BlueZ.

## Done when

- Run it on a laptop, open nRF Connect on a phone, and see the token
  rotating every 30 seconds — same as the host app.
