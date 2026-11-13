# Permissions system — feature module (not built yet)

Owns **every** runtime permission flow in both apps. No feature asks
for permissions directly — everything goes through here, so denied /
"don't ask again" / "go to settings" behaves the same everywhere.

## Public interface (what other code may call)

```kotlin
// Ask for a set of permissions, get one clear result back.
// Exact API is the team's design decision — propose it in an issue
// before building. Sketch:
PermissionManager.request(
    permissions: Set<String>,          // e.g. Bluetooth set, camera set
    rationale: String,                 // why the app needs these
    onResult: (Result) -> Unit         // Granted | Denied | PermanentlyDenied
)
PermissionManager.openAppSettings()    // for the PermanentlyDenied case
```

## Permission sets (one place, named)

| Set | Permissions | Used by |
|---|---|---|
| Bluetooth (Android 12+) | `BLUETOOTH_ADVERTISE`, `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `ACCESS_FINE_LOCATION` | BLE system |
| Bluetooth (older Android) | `ACCESS_FINE_LOCATION` | BLE system |
| Camera | `CAMERA` | QR system (scanner) |

## Rules

- Always explain *why* before asking (rationale), in plain words.
- Denied once → explain again + let the user retry. Denied permanently
  ("don't ask again") → show a message with a button to app settings.
  Never loop the system dialog, never crash, never silently do nothing.
- The system dialog result comes back through the Activity Result API —
  no deprecated `onRequestPermissionsResult` code.

## Where the code comes from

- The host app's `MainActivity` already has permission-request code.
  Extracting it into this module is a tracked issue — extract, wire
  both apps to it, test each path (granted / denied / permanently
  denied), merge.

## Done when

- Both apps request permissions only through this module.
- All three outcomes (granted, denied, permanently denied) are handled
  and demonstrable on a physical phone.
