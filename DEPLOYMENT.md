# Deploying FBGuard for free

Goal: a live URL you can put in front of a recruiter, at $0/month. This uses
three free services, verified as still offering real free tiers as of
September 2026:

| Piece | Service | Why |
|---|---|---|
| Frontend | **Vercel** | Free static hosting, instant Git deploys, custom domain |
| Backend | **Render** (free web service) | No credit card, Docker deploys, 512MB RAM |
| Database | **Aiven for MySQL** (free tier) | Genuinely free forever, 1GB RAM/storage, no card |
| Kafka | **Aiven for Apache Kafka** (free tier) | Same account as the DB, no card |

Caveat to know going in: Render's free tier spins the backend down after ~15
minutes of no traffic, so the first request after a while takes 10-30 seconds
to "wake up." That's fine for a portfolio link — just give it a moment on
first load, or see the keep-alive tip at the end.

## 1. Push the code to GitHub

```bash
cd backend && git init && git add . && git commit -m "FBGuard backend" 
# create a repo on GitHub, then:
git remote add origin https://github.com/<you>/fbguard-backend.git
git push -u origin main
```
Do the same for `frontend/` as a second repo (`fbguard-frontend`).

## 2. Create the free MySQL database (Aiven)

1. Sign up at aiven.io (no card needed).
2. Create a new service → **MySQL** → select the **Free** plan → pick any region.
3. Once it's running, open the service and copy: host, port, username,
   password, and database name from the "Connection information" panel.
4. Aiven MySQL requires SSL — when you set `DB_URL` in Render (next step),
   use:
   ```
   jdbc:mysql://<host>:<port>/<database>?sslMode=REQUIRED
   ```

## 3. Create the free Kafka cluster (Aiven)

1. In the same Aiven project, create a new service → **Apache Kafka** → **Free** plan.
2. Aiven Kafka requires TLS + SASL auth, which needs a couple of extra Spring
   properties beyond what's in `application.yml`. Simplest path for a demo
   project: **skip Kafka in production** — the app already degrades
   gracefully without it (`AppEventProducer` catches send failures and just
   logs a warning; the app still boots and works). Mention in your interview
   that the event-driven pipeline is fully implemented and demoed locally via
   `docker-compose up`, which is a completely normal thing to say about a
   student project.
   - If you do want it live, Aiven's docs show the exact
     `spring.kafka.properties.security.protocol=SASL_SSL` block to add.

## 4. Deploy the backend to Render

1. Sign up at render.com, connect your GitHub account.
2. New → Web Service → pick `fbguard-backend` repo.
3. Environment: **Docker** (Render will use the `Dockerfile` as-is).
4. Add environment variables (Render → your service → Environment):
   ```
   DB_URL=jdbc:mysql://<aiven-host>:<port>/<database>?sslMode=REQUIRED
   DB_USERNAME=<aiven-username>
   DB_PASSWORD=<aiven-password>
   JWT_SECRET=<generate a long random string>
   ADMIN_USERNAME=admin
   ADMIN_PASSWORD=<pick something real, not admin>
   PORT=8080
   # Optional - only if you want real emails (see "Email notifications" below)
   NOTIFICATIONS_ENABLED=false
   ```
   (Leave `KAFKA_BOOTSTRAP_SERVERS` unset if you're skipping Kafka in prod.)
5. Deploy. Render gives you a URL like `https://fbguard-backend.onrender.com`.
   Render's free tier supports WebSockets natively, so live chat and the
   Swagger UI (`/swagger-ui.html`) both work once deployed - no extra config.

## Optional: turn on real email notifications

The audit consumer already emails the admin on a high-risk submission and
emails the submitter when an admin approves/rejects their app - it's just off
by default so a fresh deploy never fails on missing mail credentials. To turn
it on with Gmail's free SMTP relay:

1. Turn on 2-factor auth on the Gmail account you want to send from.
2. Create an "app password" (Google Account → Security → App passwords).
3. Add these Render environment variables:
   ```
   NOTIFICATIONS_ENABLED=true
   MAIL_USERNAME=youraddress@gmail.com
   MAIL_PASSWORD=<the 16-character app password, not your real Gmail password>
   ADMIN_EMAIL=youraddress@gmail.com
   ```
Any other SMTP provider (Resend, Brevo, SendGrid's free tier) works the same
way - just change `MAIL_HOST`/`MAIL_PORT` to match.

## 5. Deploy the frontend to Vercel

1. Sign up at vercel.com, import the `fbguard-frontend` GitHub repo.
2. Framework preset: **Vite**.
3. Add environment variable:
   ```
   VITE_API_BASE_URL=https://fbguard-backend.onrender.com/api
   ```
4. Deploy. Vercel gives you a URL like `https://fbguard.vercel.app` — **this
   is the link you send to recruiters.**

## 6. (Optional) Keep the backend from sleeping

Render's free tier sleeps after 15 minutes idle. To keep it awake during a
job search, use a free uptime pinger:
- **UptimeRobot** (free) or **cron-job.org** (free) — set either to hit
  `https://fbguard-backend.onrender.com/api/apps` every 10 minutes.

## 7. Verify it works end to end

The database is pre-seeded (`data.sql`) with two demo users (`alice` /
`Demo1234!` and `bob` / `Demo1234!`), two blacklist entries, and three app
submissions — one of each status (licensed, pending, rejected) — so the
gallery, dashboard charts, and admin panel all have real data the moment a
recruiter opens the link, instead of an empty screen. Log in as `alice` to
see it, or as your admin account to review the pending one.

If you'd rather deploy with a clean database, delete
`backend/src/main/resources/data.sql` or set `spring.sql.init.mode=never`
before deploying.

1. Visit your Vercel URL, log in as `alice` / `Demo1234!` (or register a new account).
2. Submit an app with a URL like `https://apps.facebook.com/test` (should
   come back LOW risk) and one like `http://192.168.1.1/free-coins` (should
   come back HIGH risk — trips the IP-address and non-facebook-host
   heuristics).
3. Log in as your admin account, check the Admin Panel loads pending apps
   and the risk-distribution chart.

That's it — a live, working, full-stack app with a real verification engine,
for $0/month.
