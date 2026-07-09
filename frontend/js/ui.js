// Shared UI helpers: toasts, currency, star rows, a deterministic gradient for
// hotel imagery, and the top navigation bar rendered from auth state.
import { currentUser, isAuthenticated, clearTokens } from "./auth.js";
import { navigate } from "./router.js";

export const inr = (n) => "₹" + Number(n || 0).toLocaleString("en-IN");

export function toast(message, kind = "info") {
  const el = document.getElementById("toast");
  el.textContent = message;
  el.className = `toast show ${kind}`;
  clearTimeout(el._t);
  el._t = setTimeout(() => (el.className = "toast"), 3200);
}

export function stars(n) {
  return `<span class="stars">${Array.from({ length: n }, () => '<i data-lucide="star"></i>').join("")}</span>`;
}

export function gradientFor(seed = 0) {
  const hue = (seed * 47) % 360;
  return `linear-gradient(145deg, oklch(72% 0.11 ${hue}), oklch(52% 0.13 ${hue + 18}) 55%, oklch(40% 0.1 ${hue - 10}))`;
}

export function initials(text = "") {
  return text.split(/[ @.]/).filter(Boolean).map((w) => w[0]).slice(0, 2).join("").toUpperCase();
}

export function renderTopbar() {
  const bar = document.getElementById("topbar");
  const user = currentUser();
  const authed = isAuthenticated();

  const links = [
    `<a href="#/">Search</a>`,
    authed ? `<a href="#/trips">My trips</a>` : "",
    user && (user.role === "ADMIN" || user.role === "HOTEL_MANAGER")
      ? `<a href="#/admin">Operations</a>` : "",
  ].join("");

  bar.innerHTML = `
    <div class="brand" onclick="location.hash='#/'" role="button">
      <span class="brand-mark">aster<span>.</span></span>
      <span class="brand-tag">stays</span>
    </div>
    <nav>
      ${links}
      ${
        authed
          ? `<button class="btn-account" id="acct"><span class="a-dot">${initials(user?.email)}</span> ${user?.email?.split("@")[0] || "Account"}</button>`
          : `<button class="btn-account" onclick="location.hash='#/login'"><i data-lucide="user"></i> Sign in</button>`
      }
    </nav>`;

  const acct = document.getElementById("acct");
  if (acct) {
    acct.onclick = () => {
      clearTokens();
      toast("Signed out");
      navigate("/login");
    };
  }
  if (window.lucide) lucide.createIcons();
}
