# Contributing

Welcome — this project is deliberately beginner-friendly. It exists partly so
juniors can learn how a real app is built, reviewed, and shipped. You do not
need to be an expert. You do need to follow the flow below, because that flow
*is* the learning.

Working in a team? Read [`docs/TEAMS.md`](docs/TEAMS.md) first — it says
which folder your team owns and how teams work in parallel without waiting
for each other. The written contracts between parts live in
[`docs/CONTRACTS.md`](docs/CONTRACTS.md); build against those, not against
another team's unfinished code.

## The flow (read this once, then it becomes habit)

1. **Pick an issue.** Start with [`good first issue`](https://github.com/vikramnara214/dr-student/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22)
   labels. Do not start coding a big feature without an issue — it may
   duplicate someone else's work or go in a direction the maintainer does
   not want.
2. **Claim it.** Comment on the issue: "I'd like to take this." Wait until
   the maintainer assigns it to you. One person per issue.
3. **Fork the repo** (top-right Fork button), then clone *your fork*:
   ```bash
   git clone https://github.com/<your-username>/dr-student.git
   cd dr-student
   ```
4. **Create a branch** for your issue — one branch per PR:
   ```bash
   git checkout -b fix/splash-translation-123   # or feat/student-qr-scan-45
   ```
   Name it `fix/...`, `feat/...`, or `docs/...` + a short description + the
   issue number.
5. **Make the change small.** One issue = one PR. If you spot something else
   while working, open a separate issue instead of bundling it in.
6. **Test what you can.** For Android changes: does it compile, does the app
   still run? For the beacon: does the token still rotate every 30s? Say in
   the PR what you tested and on what (phone model / emulator).
7. **Open a Pull Request** from your branch to `main` in this repo. Fill in
   the PR template: what changed, which issue it closes (`Closes #123`),
   and how you tested it.
8. **Review.** The maintainer reviews every PR. Expect comments — that is
   normal and it is not a rejection. Reply, make the requested changes on
   the same branch, and the PR updates automatically. Once approved, the
   maintainer merges it.

## How reviews work here

- The maintainer (`@vikramnara214`) reviews and merges all PRs. Nobody
  merges their own PR.
- A review comment is a question or a suggestion, not a grade. If you do not
  understand a comment, ask on the PR — do not guess and re-submit silently.
- Keep PRs small and focused: a 50-line PR gets reviewed fast, a 500-line
  one waits.

## Code style

- Kotlin for Android, Rust for `server/` and `rust/token-core/`.
- Follow the existing style in the file you are editing. Run the formatter
  your IDE offers before committing.
- No secrets, API keys, or personal data in the code — ever. Attendance
  data in tests must be fake.
- Write a clear commit message: `Fix splash translation (#537)` beats `fix`.

## First-time contributors

- Never contributed before? Start with a docs fix or the smallest
  `good first issue`. Your first merged PR matters more than a big one.
- Stuck? Ask on the issue — do not stay stuck for days. "I tried X, I got
  error Y" is a perfect question.
- Be kind and patient in reviews, both giving and receiving them.

## What not to do

- Do not push directly to `main` — all changes go through a PR.
- Do not open a PR without an issue unless it is a tiny typo/docs fix.
- Do not add a dependency without asking on the issue first.
