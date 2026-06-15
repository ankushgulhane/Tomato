# Tomato

Online food delivery app: Spring Boot REST API (`/src`) + React/Vite UI (`/UI`).

## Running locally

**Backend** (from the project root):

```bash
mvn spring-boot:run
```

Starts on `http://localhost:8080` with an in-memory H2 database, seeded with demo
accounts (see below).

**Frontend** (from `/UI`):

```bash
npm install
npm run dev
```

Starts on `http://localhost:5173` and proxies `/api/*` to the backend.

## Demo accounts (local development only)

The seeder creates these accounts with the password `password123`:

| Email | Role |
|---|---|
| admin@tomato.com | ADMIN |
| alice@tomato.com | USER |
| bob@tomato.com | RESTAURANT |
| mei@tomato.com | RESTAURANT |
| carol@tomato.com | DELIVERY_PARTNER |

## Creating restaurant, delivery partner, or admin accounts

Self-registration (`/register`) only creates **customer (USER)** accounts. Other
roles must be created by an admin:

1. Log in as an ADMIN account (e.g. `admin@tomato.com` in dev).
2. Open **Create User** in the nav bar (`/admin/users/new`).
3. Fill in the new user's name, email, a temporary password, and pick their role
   (Restaurant Owner, Delivery Partner, or Admin).
4. Submit. The account is created immediately and the form resets so you can add
   more.
5. Give the new user their email/temporary password. They log in normally at
   `/login`. Restaurant owners then complete `/restaurant/onboard` to register
   their restaurant.

## Deploying to the internet

The defaults in `application.properties` are safe for local development but
**must be overridden via environment variables** for any deployment reachable
from the internet:

| Variable | Purpose | Required value in production |
|---|---|---|
| `TOMATO_JWT_SECRET` | Signs/verifies login tokens | Long random value, e.g. `openssl rand -base64 48`. Anyone who knows this can forge tokens for any user, including ADMIN. |
| `TOMATO_H2_CONSOLE_ENABLED` | Enables the `/h2-console` web UI | Leave unset / `false`. Enabling it exposes full SQL access to the database. |
| `TOMATO_SEED_DATA_ENABLED` | Seeds demo accounts with a public password | Set to `false`. |
| `TOMATO_LOG_LEVEL` | Log verbosity for `com.tomato` | `INFO` or `WARN`, to avoid leaking sensitive data into logs. |
| `TOMATO_AUTH_RATE_LIMIT_MAX` / `TOMATO_AUTH_RATE_LIMIT_WINDOW_SECONDS` | Login/register rate limit per IP | Defaults (10 requests / 60s) are reasonable; tune as needed. |
| `TOMATO_CORS_ALLOWED_ORIGINS` | Comma-separated origins allowed to call the API from a browser | Only set if the UI is served from a different origin than the API (e.g. `https://app.example.com`). Leave unset for same-origin deployments. |

Also note:

- The app uses an **in-memory H2 database** — all data is lost on restart. Replace
  `spring.datasource.*` in `application.properties` with a persistent database
  (e.g. Postgres) before relying on this for real data.
- If running behind a TLS-terminating reverse proxy (nginx, Cloudflare, etc.),
  `server.forward-headers-strategy=framework` is already set so HTTPS is detected
  correctly (needed for the HSTS header).
