// Tiny hash router. Routes are (path -> render fn). Guards redirect unauthenticated
// or unauthorized users. Keeps the app a true SPA with zero dependencies.
import { isAuthenticated, hasRole } from "./auth.js";

const routes = [];

export function route(pattern, render, guard) {
  // pattern like "/hotels/:id" -> regex with named groups
  const keys = [];
  const regex = new RegExp(
    "^" + pattern.replace(/:([A-Za-z]+)/g, (_, k) => {
      keys.push(k);
      return "([^/]+)";
    }) + "$"
  );
  routes.push({ regex, keys, render, guard });
}

export function navigate(path) {
  if (location.hash.slice(1) === path) resolve();
  else location.hash = path;
}

export function currentPath() {
  return location.hash.slice(1) || "/";
}

async function resolve() {
  const path = currentPath();
  const app = document.getElementById("app");

  for (const r of routes) {
    const m = path.match(r.regex);
    if (!m) continue;

    if (r.guard) {
      const ok = r.guard({ authed: isAuthenticated(), hasRole });
      if (ok !== true) {
        navigate(ok || "/login");
        return;
      }
    }

    const params = {};
    r.keys.forEach((k, i) => (params[k] = decodeURIComponent(m[i + 1])));
    app.innerHTML = '<div class="loading">Loading…</div>';
    try {
      await r.render(app, params);
    } catch (e) {
      app.innerHTML = `<div class="error-state"><h2>Something broke</h2><p>${e.message}</p></div>`;
    }
    if (window.lucide) lucide.createIcons();
    window.scrollTo(0, 0);
    return;
  }

  app.innerHTML = '<div class="error-state"><h2>Not found</h2><p>No page at this address.</p></div>';
}

export function startRouter() {
  window.addEventListener("hashchange", resolve);
  resolve();
}
