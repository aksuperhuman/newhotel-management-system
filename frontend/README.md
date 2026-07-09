# Aster — Frontend

A dependency-free single-page app for the Hotel Reservation Engine. Vanilla ES
modules, hash routing, JWT auth with silent refresh, wired to the Spring Boot API.
No build step, no framework: it runs as-is when served over HTTP.

## What’s here

| Area | Route | Notes |
|------|-------|-------|
| Search | `#/` | Public hotel search (city, dates, rating) |
| Hotel + booking | `#/hotels/:id` | Room list + booking drawer showing the lock → payment → confirmed lifecycle |
| My trips | `#/trips` | Auth required; cancel releases inventory |
| Operations | `#/admin` | ADMIN / HOTEL_MANAGER only; live dashboard metrics |
| Auth | `#/login` | Sign in + register (role-aware) |

## Structure

```
frontend/
├── index.html          # shell + module entrypoint
├── css/styles.css      # full design system (clay + paper, Fraunces/Inter)
├── js/
│   ├── app.js          # routes + guards + bootstrap
│   ├── config.js       # API base, roles, gateways
│   ├── api.js          # fetch wrapper: JWT header, error parsing, 401 refresh
│   ├── auth.js         # token storage, JWT decode, role checks
│   ├── router.js       # tiny hash router with route guards
│   ├── ui.js           # toast, currency, topbar, shared bits
│   └── views/          # login, search, hotel, trips, admin
├── nginx.conf          # SPA fallback + /api reverse proxy to the backend
└── Dockerfile          # nginx static serve
```

## Run it

### With the backend (recommended)
Add this service to the root `docker-compose.yml` and it proxies `/api` to the app:

```yaml
  web:
    build: ./frontend
    container_name: hre-web
    depends_on:
      - app
    ports:
      - "5173:80"
```

Then open http://localhost:5173. Seed logins (password `password123`):
`admin@hotel.com`, `manager@hotel.com`, `customer@hotel.com`.

### Standalone (any static server)
```bash
cd frontend
python3 -m http.server 5173
```
Set `window.__API_BASE__` in `index.html` if the API isn’t reachable at `/api/v1`.

## Design

Warm editorial palette (clay accent on paper neutrals, OKLCH throughout),
Fraunces for display + Inter for UI. The booking drawer visualizes the backend’s
reservation lifecycle so the concurrency story is legible to a human.
