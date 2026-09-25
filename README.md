# FBGuard — Malicious Facebook App Detection Platform

## What this project does

FBGuard is a full-stack web application that simulates how a social
platform vets third-party apps before letting users install them.

A user can:
- Register and log in
- Add friends and chat with them in real time
- Submit a "Facebook app" (a name + a URL) to be listed in a public gallery
- Get that app automatically scored for risk the moment it's submitted —
  checked against a blacklist, scanned for suspicious URL patterns, checked
  against Google's Safe Browsing database, and checked for how recently its
  domain was registered
- See the result instantly: safe apps get auto-approved, clearly malicious
  ones get auto-blocked, and anything in between waits for a human admin to
  review it

An admin has a separate panel to review those pending apps, maintain a
blacklist, and see live charts of submission activity and risk levels
across the whole platform.

## How it's helpful

Third-party apps on social platforms have historically been a common way
for scams, data-harvesting tools, and malware to reach users — a fake quiz
app or a "free coins" game asking for account access is a real, recurring
problem. This project demonstrates a working, automated way to catch that
kind of thing *before* it reaches users, instead of relying on a single
static rule:

- **Multiple independent checks combined into one score** — rather than
  trusting one blacklist, it weighs four separate signals (known-bad list,
  URL structure red flags, Google's own threat database, and domain age) so
  a new scam that isn't blacklisted yet can still get caught by how
  suspicious its URL looks or how recently it was registered.
- **Automatic decisions where confidence is high** — genuinely safe or
  genuinely dangerous apps are handled instantly, with no human needed.
- **Human review only where it's actually needed** — borderline cases are
  queued for an admin, so people aren't reviewing thousands of obviously-fine
  submissions.
- **A visible audit trail** — every score comes with an explanation of which
  checks fired and why, so a decision is never a black box.

Beyond the app-vetting feature itself, the project is built with the same
practical concerns a real, live system has to handle: keeping user
passwords properly hashed, not letting one user spam the system, keeping the
app fast as data grows, and making sure a feature failing (like a missing
API key) never crashes the whole platform — it just degrades gracefully.

## What's under the hood

| Piece | What it's for |
|---|---|
| **Spring Boot (Java) backend** | Handles all business logic, authentication, and database access through a clean, layered structure |
| **React frontend** | A responsive, real-time UI — pages update instantly based on live data instead of full page reloads |
| **MySQL** | Stores users, apps, messages, friendships, and the blacklist permanently |
| **JWT authentication** | Lets the frontend securely prove who's logged in on every request, with passwords stored using one-way BCrypt hashing, never in plain text |
| **Multi-signal verification engine** | The core feature — combines blacklist checks, URL heuristics, Google Safe Browsing, and domain-age lookups into one weighted risk score |
| **Kafka** | Lets other things react to an app submission (logging, emailing an admin) without that logic being tangled into the submission code itself |
| **Caching** | Avoids hitting the database on every page load for data that barely changes minute to minute |
| **Rate limiting** | Stops one user (or a bot) from spamming submissions |
| **Pagination** | Keeps the app gallery fast even as it grows to thousands of entries |
| **WebSockets** | Makes chat feel instant — messages appear the moment they're sent, no refresh needed |
| **Docker** | Starts the entire stack (database, message queue, backend, frontend) with one command, identically on any machine |

## Running it locally

```bash
docker compose up --build
```
- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Demo accounts: `alice` / `Demo1234!`, `bob` / `Demo1234!`
- Admin: `admin` / whatever `ADMIN_PASSWORD` is set to

See `DEPLOYMENT.md` for putting this online for free, and
