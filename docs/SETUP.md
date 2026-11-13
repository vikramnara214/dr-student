# Setup — developing and testing

## What you need

- Android Studio (latest stable), with an Android SDK.
- A **physical Android phone** to run the host app — emulators cannot
  advertise BLE. A second phone (any Android/iPhone) with **nRF Connect**
  (free) to watch the beacon.
- Git, JDK 17+ (Android Studio bundles one).

## Host app (the part that exists today)

```bash
git clone https://github.com/vikramnara214/dr-student.git
cd dr-student
```

1. Android Studio → Open → `android/host-app`. Let Gradle sync (needs
   internet once, for the ZXing dependency).
2. Run on a physical phone. Grant Bluetooth + Location permissions when
   asked — BLE advertising needs `BLUETOOTH_ADVERTISE`, and Android
   requires location permission for BLE.
3. Tap Start session. A QR appears and the beacon starts.
4. On the second phone, nRF Connect → Scanner → look for service UUID
   `b5a3f2c1-4d6e-4a8b-9c0d-1e2f3a4b5c6d`. The service data shows an
   8-character hex token that should change every 30 seconds.

If it does not compile, fix the compiler complaint first — expect a
manifest / dependency nit, not a BLE-logic problem. The starter was
delivered uncompiled.

## Developing on a laptop (Pop!_OS / Linux)

You do most development on the laptop; you only need a phone for the radio.

- **Emulator:** Android Studio AVD with KVM. Good for UI, QR scan/generate,
  token maths, and server calls — everything except the radio.
- **BLE inside the emulator:** the real option is **Bumble**, Google's
  Python Bluetooth stack, which bridges the emulator to a USB Bluetooth
  dongle or a virtual BLE device. Fiddly; use it only if you need BLE
  *inside* the emulator.
- **Fast path — laptop as beacon:** the laptop's own Bluetooth can advertise
  the token (Rust, `bluer` crate — see `rust/token-core/`). Then the
  student app in the emulator can hear a real beacon without a second phone.

## Calibrating RSSI

Walk to the back of the room with nRF Connect open and note the RSSI of
the beacon. That number is your starting threshold for that room — the
−75 dBm figure in the docs is a starting point, not a universal answer.
