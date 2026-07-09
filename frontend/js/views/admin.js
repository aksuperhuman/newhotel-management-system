import { api } from "../api.js";
import { inr } from "../ui.js";

export async function adminView(app) {
  app.innerHTML = `<section class="page"><h1 class="page-title">Operations</h1><div id="dash" class="dash"></div></section>`;
  const dash = app.querySelector("#dash");
  try {
    const d = await api.dashboard();
    dash.innerHTML = `
      <div class="kpi-row">
        ${kpi("Revenue", inr(d.totalRevenue), "indian-rupee")}
        ${kpi("Active reservations", d.activeReservations, "calendar-check")}
        ${kpi("Occupancy", pct(d.occupancyRate), "activity")}
        ${kpi("Cancellation rate", pct(d.cancellationRate), "x-circle")}
      </div>
      <div class="kpi-row">
        ${kpi("Total hotels", d.totalHotels, "building-2")}
        ${kpi("Total rooms", d.totalRooms, "bed-double")}
      </div>
      <div class="panel">
        <div class="panel-head"><h3>Most booked hotels</h3></div>
        <div class="rank-list">
          ${(d.mostBookedHotels || []).length
            ? d.mostBookedHotels.map((h, i) => rank(h, i, d.mostBookedHotels[0]?.bookings || 1)).join("")
            : '<div class="empty sm"><p>No booking data yet.</p></div>'}
        </div>
      </div>`;
    if (window.lucide) lucide.createIcons();
  } catch (err) {
    dash.innerHTML = `<div class="error-state"><h3>Dashboard unavailable</h3><p>${err.message}</p></div>`;
  }
}

const pct = (v) => `${Math.round((v || 0) * 100)}%`;

function kpi(label, value, icon) {
  return `<div class="kpi"><div class="kpi-label"><i data-lucide="${icon}"></i> ${label}</div><div class="kpi-val">${value}</div></div>`;
}

function rank(h, i, max) {
  return `<div class="rank"><div class="num">${i + 1}</div><div class="r-body">
    <div class="r-top"><span class="r-name">${h.name}</span><span class="r-count">${h.bookings} bookings</span></div>
    <div class="bar"><i style="width:${Math.max(6, (h.bookings / max) * 100)}%"></i></div></div></div>`;
}
