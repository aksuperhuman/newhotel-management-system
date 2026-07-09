import { api } from "../api.js";
import { inr, toast } from "../ui.js";

const STATUS_CLASS = {
  CONFIRMED: "confirmed", PAYMENT_PENDING: "payment", CHECKED_IN: "checked",
  CHECKED_OUT: "checked", COMPLETED: "confirmed", CANCELLED: "cancelled", PENDING: "payment",
};

export async function tripsView(app) {
  app.innerHTML = `<section class="page"><h1 class="page-title">My trips</h1><div id="list"></div></section>`;
  const list = app.querySelector("#list");
  try {
    const trips = await api.myReservations();
    if (!trips.length) {
      list.innerHTML = `<div class="empty"><i data-lucide="luggage"></i><h3>No trips yet</h3><p>Your booked stays will show up here.</p><a class="btn-primary" href="#/">Find a stay</a></div>`;
      lucide.createIcons();
      return;
    }
    list.innerHTML = trips.map(tripCard).join("");
    lucide.createIcons();
    list.querySelectorAll("[data-cancel]").forEach((b) =>
      b.addEventListener("click", async () => {
        b.disabled = true;
        try {
          await api.cancel(b.dataset.cancel);
          toast("Reservation cancelled, inventory released", "success");
          tripsView(app);
        } catch (err) {
          toast(err.message, "error");
          b.disabled = false;
        }
      })
    );
  } catch (err) {
    list.innerHTML = `<div class="error-state"><h3>Couldn’t load your trips</h3><p>${err.message}</p></div>`;
  }
}

function tripCard(t) {
  const canCancel = ["CONFIRMED", "PAYMENT_PENDING", "PENDING"].includes(t.status);
  return `
    <div class="trip">
      <div class="trip-main">
        <div class="trip-dates">${t.checkIn} → ${t.checkOut}</div>
        <div class="trip-sub">Reservation #${t.id} · ${t.guests} guests</div>
      </div>
      <div class="trip-amt">${inr(t.totalAmount)}</div>
      <span class="status ${STATUS_CLASS[t.status] || "payment"}">${t.status.replace(/_/g, " ")}</span>
      ${canCancel ? `<button class="btn-ghost" data-cancel="${t.id}">Cancel</button>` : `<span class="trip-spacer"></span>`}
    </div>`;
}
