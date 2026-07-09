// Central config. Override API_BASE at build/deploy time if the API lives elsewhere.
// When served by nginx alongside the app, /api is reverse-proxied to the backend.
export const API_BASE = window.__API_BASE__ || "/api/v1";

export const ROLES = {
  CUSTOMER: "CUSTOMER",
  HOTEL_MANAGER: "HOTEL_MANAGER",
  ADMIN: "ADMIN",
};

export const GATEWAYS = ["STRIPE", "RAZORPAY", "PAYPAL"];
