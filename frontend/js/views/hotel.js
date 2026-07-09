import { api } from "../api.js";
import { inr, stars, gradientFor, toast } from "../ui.js";
import { isAuthenticated } from "../auth.js";
import { navigate } from "../router.js";
import { GATEWAYS } from "../config.js";

export async function hotelView(app, { id }) {
  const [hotel, rooms] = await Promise.all([api.getHotel(id), api.listRooms(id)]);

  app.innerHTML = `
    <div class="hotel-hero" style="background:${gradientFor(hotel.id)}">
      <div class="hotel-hero-body">
        <a class="back" href="#/"><i data-lucide="arrow-left"></i> All stays</a>
        <h1>${hotel.name}</h1>
        <div class="hotel-meta">${stars(hotel.starRating || 0)} <span>· ${hotel.address || hotel.city}</span></div>
      </div>
    </div>
    <section class="hotel-body">
      <p class="hotel-desc">${hotel.description || ""}</p>
      <h3 class="section-title">Rooms</h3>
      <div class="room-list" id="rooms">
        ${rooms.map(roomRow).join("") || '<div class="empty"><h3>No rooms listed yet</h3></div>'}
      </div>
    </section>

    <div class="scrim" id="scrim"></div>
    <aside class="drawer" id="drawer" aria-hidden="true"></aside>`;

  lucide.createIcons();

  app.querySelectorAll("[data-book]").forEach((btn) =>
    btn.addEventListener("click", () => {
      if (!isAuthenticated()) {
        toast("Sign in to book", "info");
        navigate("/login");
        return;
      }
      openDrawer(hotel, rooms.find((r) => String(r.id) === btn.dataset.book));
    })
  );
}

function roomRow(r) {
  return `
    <div class="room-row">
      <div>
        <div class="room-type">${r.roomType}</div>
        <div class="room-feat">${r.features || ""} · sleeps ${r.capacity}</div>
        <div class="room-status ${r.status === "AVAILABLE" ? "ok" : "off"}">${r.status}</div>
      </div>
      <div class="room-cta">
        <div class="room-price">${inr(r.price)}<small>/night</small></div>
        <button class="btn-primary" data-book="${r.id}" ${r.status !== "AVAILABLE" ? "disabled" : ""}>Book</button>
      </div>
    </div>`;
}

function openDrawer(hotel, room) {
  const scrim = document.getElementById("scrim");
  const drawer = document.getElementById("drawer");
  const today = new Date();
  const iso = (d) => d.toISOString().slice(0, 10);
  const inDefault = iso(new Date(today.getTime() + 7 * 864e5));
  const outDefault = iso(new Date(today.getTime() + 9 * 864e5));

  drawer.innerHTML = `
    <div class="drawer-head">
      <div><h3>${hotel.name}</h3><div class="sub">${room.roomType} · ${inr(room.price)}/night</div></div>
      <button class="x" id="close"><i data-lucide="x"></i></button>
    </div>
    <div class="drawer-body">
      <label class="fld">Check in<input type="date" id="ci" value="${inDefault}" /></label>
      <label class="fld">Check out<input type="date" id="co" value="${outDefault}" /></label>
      <label class="fld">Guests<input type="number" id="g" min="1" max="${room.capacity}" value="2" /></label>
      <div class="fld"><span>Payment</span><div class="pays" id="pays">
        ${GATEWAYS.map((g, i) => `<button class="pay ${i === 0 ? "sel" : ""}" data-g="${g}">${g[0] + g.slice(1).toLowerCase()}</button>`).join("")}
      </div></div>
      <div class="lifecycle" id="life">
        <div class="lc pending"><span class="d"></span> Room locked</div>
        <div class="lc pending"><span class="d"></span> Payment</div>
        <div class="lc pending"><span class="d"></span> Confirmed</div>
      </div>
    </div>
    <div class="drawer-foot">
      <div class="sum"><span id="sum-desc">${room.roomType} · 2 nights</span><strong id="sum-total">${inr(room.price * 2)}</strong></div>
      <button class="btn-primary lg" id="confirm">Lock room &amp; pay</button>
    </div>`;

  scrim.classList.add("open");
  drawer.classList.add("open");
  drawer.setAttribute("aria-hidden", "false");
  lucide.createIcons();

  let gateway = GATEWAYS[0];
  const close = () => {
    scrim.classList.remove("open");
    drawer.classList.remove("open");
  };
  scrim.onclick = close;
  drawer.querySelector("#close").onclick = close;

  const ci = drawer.querySelector("#ci");
  const co = drawer.querySelector("#co");
  const recalc = () => {
    const nights = Math.max(1, Math.round((new Date(co.value) - new Date(ci.value)) / 864e5));
    drawer.querySelector("#sum-desc").textContent = `${room.roomType} · ${nights} nights`;
    drawer.querySelector("#sum-total").textContent = inr(room.price * nights);
  };
  ci.onchange = recalc;
  co.onchange = recalc;

  drawer.querySelectorAll(".pay").forEach((p) =>
    p.addEventListener("click", () => {
      drawer.querySelectorAll(".pay").forEach((x) => x.classList.remove("sel"));
      p.classList.add("sel");
      gateway = p.dataset.g;
    })
  );

  drawer.querySelector("#confirm").addEventListener("click", async function () {
    const btn = this;
    btn.disabled = true;
    const lcs = drawer.querySelectorAll(".lc");
    lcs[0].className = "lc active";
    btn.innerHTML = 'Locking room…';
    try {
      const payload = {
        roomId: room.id,
        checkIn: ci.value,
        checkOut: co.value,
        guests: Number(drawer.querySelector("#g").value),
        gateway,
      };
      lcs[1].className = "lc active";
      btn.innerHTML = `Processing ${gateway[0] + gateway.slice(1).toLowerCase()}…`;
      const res = await api.book(payload);
      if (res.status === "CONFIRMED") {
        lcs.forEach((l) => (l.className = "lc done"));
        btn.innerHTML = "Confirmed";
        toast("Booking confirmed", "success");
        setTimeout(() => navigate("/trips"), 900);
      } else {
        toast(`Reservation ${res.status.toLowerCase()}`, "info");
        btn.disabled = false;
        btn.innerHTML = "Try again";
      }
    } catch (err) {
      toast(err.message, "error");
      btn.disabled = false;
      btn.innerHTML = "Lock room & pay";
    }
  });
}
