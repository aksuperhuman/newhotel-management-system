// JWT + session handling. Tokens live in localStorage; the JWT payload is decoded
// client-side purely to render role-aware UI (never trusted for authorization,
// the backend enforces that via @PreAuthorize).
const KEY = "aster.tokens";
const listeners = new Set();

export function getTokens() {
  try {
    return JSON.parse(localStorage.getItem(KEY));
  } catch (_) {
    return null;
  }
}

export function setTokens(tokens) {
  localStorage.setItem(KEY, JSON.stringify(tokens));
  listeners.forEach((fn) => fn());
}

export function clearTokens() {
  localStorage.removeItem(KEY);
  listeners.forEach((fn) => fn());
}

export function onAuthChange(fn) {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

export function isAuthenticated() {
  return !!getTokens()?.accessToken;
}

// Decode the JWT body (base64url) to read subject + role for UI purposes.
export function currentUser() {
  const t = getTokens();
  if (!t?.accessToken) return null;
  try {
    const payload = JSON.parse(atob(t.accessToken.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    return { email: payload.sub, role: payload.role };
  } catch (_) {
    return null;
  }
}

export function hasRole(...roles) {
  const u = currentUser();
  return !!u && roles.includes(u.role);
}
