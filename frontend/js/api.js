// Thin fetch wrapper: attaches the JWT, parses JSON, surfaces API errors,
// and transparently refreshes the access token once on a 401.
import { API_BASE } from "./config.js";
import { getTokens, setTokens, clearTokens } from "./auth.js";

async function raw(path, { method = "GET", body, auth = true, _retried = false } = {}) {
  const headers = { "Content-Type": "application/json" };
  const tokens = getTokens();
  if (auth && tokens?.accessToken) {
    headers.Authorization = `Bearer ${tokens.accessToken}`;
  }

  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  // Try a single silent refresh on expiry, then replay the request.
  if (res.status === 401 && auth && !_retried && tokens?.refreshToken) {
    const refreshed = await tryRefresh(tokens.refreshToken);
    if (refreshed) return raw(path, { method, body, auth, _retried: true });
    clearTokens();
  }

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const err = await res.json();
      message = err.message || message;
    } catch (_) {}
    throw new ApiError(message, res.status);
  }

  if (res.status === 204) return null;
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

async function tryRefresh(refreshToken) {
  try {
    const res = await fetch(`${API_BASE}/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });
    if (!res.ok) return false;
    const data = await res.json();
    setTokens(data);
    return true;
  } catch (_) {
    return false;
  }
}

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}

export const api = {
  // Auth
  register: (payload) => raw("/auth/register", { method: "POST", body: payload, auth: false }),
  login: (payload) => raw("/auth/login", { method: "POST", body: payload, auth: false }),

  // Search (public)
  searchHotels: (criteria, page = 0, size = 20) =>
    raw(`/hotels/search?page=${page}&size=${size}`, { method: "POST", body: criteria, auth: false }),

  // Hotels / rooms
  getHotel: (id) => raw(`/hotels/${id}`, { auth: false }),
  listRooms: (hotelId) => raw(`/hotels/${hotelId}/rooms`, { auth: false }),
  createHotel: (payload) => raw("/hotels", { method: "POST", body: payload }),
  addRoom: (hotelId, payload) => raw(`/hotels/${hotelId}/rooms`, { method: "POST", body: payload }),

  // Reservations
  book: (payload) => raw("/reservations", { method: "POST", body: payload }),
  myReservations: () => raw("/reservations/me"),
  cancel: (id) => raw(`/reservations/${id}/cancel`, { method: "POST" }),

  // Admin
  dashboard: () => raw("/admin/dashboard"),
};
