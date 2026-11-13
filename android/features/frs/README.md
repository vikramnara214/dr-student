# FRS — Face Recognition System (feature module, not built yet)

Verifies **the person** at login. QR + BLE prove the *phone* is in the
room; FRS proves the person holding it is the student they claim to be.
Without it, a friend could log in as you on your phone and take your
attendance. With it, login itself is the identity check.

## Where it sits in the flow

```
Student opens app
  → enters studentId (roll number)
  → FRS: face capture → compare with enrolled face
  → match → logged in → QR scan + BLE + submit attendance
  → no match → login fails (retry, then host/manual check)
```

FRS runs **at login, before anything else**. The student app must not
submit attendance unless FRS login passed for this session.

## How it works (on-device only)

1. **Enrol once** (first setup, guided): the student captures their
   face; the module computes a face *embedding* (a vector of numbers
   describing the face) and stores it on the phone.
2. **Login**: capture the face again, compute the embedding, compare
   with the enrolled one by cosine similarity. Above the threshold →
   match.
3. **No photos are kept.** Only the embedding is stored, on-device,
   encrypted (Android Keystore / encrypted storage). No face photo or
   embedding ever leaves the phone — the server never sees any face
   data, and FRS needs no network at all.

## Public interface (what other code may call)

```kotlin
// Enrol the current user (first setup). Returns success or a clear error.
Frs.enrol(onResult: (Result<Unit>) -> Unit)

// Verify the person in front of the camera against the enrolled face.
Frs.verify(onResult: (FrsResult) -> Unit)
// FrsResult = Match | NoMatch | NoEnrolment | CameraError

// Delete the enrolled face (account reset / user request).
Frs.deleteEnrolment()
```

Exact API is the team's design decision — propose it in an issue before
building. The three operations above (enrol / verify / delete) are the
contract: the app needs nothing else from this module.

## Technology (team's choice, within these bounds)

- Face **detection** (finding a face in the frame): ML Kit face
  detection is the expected tool.
- Face **recognition** (is this the same person?): detection alone
  cannot do this — it needs an embedding model run on-device with
  TensorFlow Lite (e.g. a MobileFaceNet-style model). The model file
  lives in this module; the team picks the exact model in an issue
  before building, and documents its licence + size.
- Similarity threshold: starting point **0.6 cosine similarity**,
  tunable — the team must test it with real faces (same person in
  different light / with glasses, different people) and report the
  false-accept / false-reject behaviour in the PR.
- Camera permission goes through the permissions system — this module
  never asks for permissions directly.

## Privacy — read this before writing code

- Face data is biometric data. Handle it like it matters, because it does.
- On-device only. Nothing biometric is uploaded, logged, or synced.
- No face photos are stored — embeddings only, encrypted at rest.
- The student can delete their enrolment at any time (`deleteEnrolment`).
- The app must tell the student, in plain words, what is stored and
  where, before the first enrolment. That text is part of this
  module's work, not an afterthought.

## Known limits (documented, not bugs)

- **Photo spoofing:** v1 has no liveness check, so holding up a photo
  of the student could pass. Same honesty rule as the BLE relay limit:
  v1 stops casual proxy login, not a determined spoof. A liveness
  check (blink / head-turn prompt) is a tracked follow-up, not v1.
- **Server cannot verify FRS in v1.** The login gate runs in the app;
  the server trusts the `studentId` in the submission. A modified
  client could bypass it — that is the same trust model as any
  client-side check, and it is why FRS is one layer (identity) on top
  of QR + BLE (presence), not the whole system.
- Lighting, glasses, and camera quality change accuracy. The threshold
  must be tested on real phones, not just the emulator (whose camera is
  fake).

## Fallback when FRS fails

After 3 failed login attempts, the app locks FRS login and tells the
student to ask the host/teacher for a manual check. There is no
self-serve bypass — a bypass would defeat the point. The manual check
is a human decision, and the host records it.

## Done when

- Enrol → verify same person (match) → verify a different person
  (no match) all work on a physical phone.
- No face data leaves the phone; no photos stored; enrolment deletable.
- The module builds and its tests pass on their own, without the apps.
