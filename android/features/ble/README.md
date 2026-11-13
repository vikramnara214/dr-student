# Bluetooth system — feature module (in progress)

Owns everything BLE: advertising the rotating token (host) and scanning
for it + measuring RSSI (student). One team, one module, both apps use it.

## Public interface (what other code may call)

```kotlin
// Host side — advertise a token, stop advertising.
BleAdvertiser.start(token: String, onError: (String) -> Unit)
BleAdvertiser.stop()
BleAdvertiser.isAdvertising(): Boolean
BleAdvertiser.canAdvertise(): Boolean

// Student side — scan for our beacon, report token + RSSI.
// Exact scanner API is the team's design decision — propose it in an
// issue before building.
BleScanner.start(onHeard: (token: String, rssi: Int) -> Unit)
BleScanner.stop()
```

## Contract

- Service UUID `b5a3f2c1-4d6e-4a8b-9c0d-1e2f3a4b5c6d`, service data is the
  8-char token as UTF-8, non-connectable, 31-byte ad limit (contract §2).
- The advertiser re-advertises whenever the 30s window rolls over.
- Bluetooth permission goes through the permissions system — this
  module never asks for permissions directly.
- If Bluetooth is off or advertising is unsupported, report a clean
  error through the callback — never crash (see issue #2).

## Where the code comes from

- The advertiser already exists as `BleAdvertiser.kt` in the host app.
  Moving it here is a tracked issue — move, wire the host app to this
  module, test, merge.
- The scanner is new work (student app). Test it against the laptop
  beacon (`devtools/laptop-beacon/`) or a host phone running the app —
  and against nRF Connect readings to sanity-check RSSI.

## Done when

- Host app advertises via this module; token rotates every 30s.
- Student side hears the token and reports RSSI.
- The module builds and its tests pass on their own, without the apps.
  (BLE radio tests need a physical phone — say so in the PR.)
