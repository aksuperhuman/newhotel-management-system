import { api } from "../api.js";
import { inr, stars, gradientFor, toast } from "../ui.js";
import { navigate } from "../router.js";

const ROOM_TYPES = ["", "STANDARD", "DELUXE", "PREMIUM", "SUITE"];

export async function searchView(app) {
  app.innerHTML = `
    <section class="hero">
      <div class="hero-eyebrow">Curated stays across India</div>
      <h1>Find a room that’s <em>actually</em> available.</h1>
      <form class="search" id="search-form">
        <label class="field"><span>Where</span><input name="city" value="Bengaluru" placeholder="City" /></label>
        <label class="field"><span>Check in</span><input name="checkIn" type="date" /></label>
        <label class="field"><span>Check out</span><input name="checkOut" type="date" /></label>
        <label class="field"><span>Min rating</span>
          <select name="starRating"><option value="">Any</option><option>3</option><option>4</option><option>5</option></select>
        </label>
        <button class="search-go" type="submit"><i data-lucide="search"></i> Search</button>
      </form>
    </section>
    <section class="results-wrap">
      <div class="result-head"><div class="count" id="count">Searching…</div></div>
      <div class="stays" id="stays"></div>
    </section>`;

  const form = app.querySelector("#search-form");
  form.addEventListener("submit", (e) => {
    e.preventDefault();
    runSearch(form);
  });
  runSearch(form);
}

async function runSearch(form) {
  const raw = Object.fromEntries(new FormData(form));
  const criteria = {
    city: raw.city || null,
    checkIn: raw.checkIn || null,
    checkOut: raw.checkOut || null,
    starRating: raw.starRating ? Number(raw.starRating) : null,
  };
  const stays = document.getElementById("stays");
  const count = document.getElementById("count");
  stays.innerHTML = skeletons(3);

  try {
    const page = await api.searchHotels(criteria);
    const hotels = page?.content ?? [];
    count.innerHTML = `<strong>${page?.totalElements ?? hotels.length}</strong> stays found`;
    if (!hotels.length) {
      stays.innerHTML = `<div class="empty"><i data-lucide="telescope"></i><h3>No stays match</h3><p>Try widening your dates or dropping the rating filter.</p></div>`;
      lucide.createIcons();
      return;
    }
    stays.innerHTML = hotels.map(card).join("");
    lucide.createIcons();
  } catch (err) {
    toast(err.message, "error");
    stays.innerHTML = `<div class="error-state"><h3>Couldn’t load stays</h3><p>${err.message}</p></div>`;
  }
}

function card(h) {
  return `
    <article class="stay" onclick="location.hash='#/hotels/${h.id}'">
      <div class="stay-img"><div class="ph" style="background:${gradientFor(h.id)}"></div>
        <span class="badge">${h.city}</span></div>
      <div class="stay-body">
        <div class="stay-top">
          <div><div class="stay-name">${h.name}</div>
            <div class="stay-loc"><i data-lucide="map-pin"></i> ${h.address || h.city}</div></div>
          ${stars(h.starRating || 0)}
        </div>
        <p class="stay-desc">${h.description || ""}</p>
        <div class="stay-foot">
          <span class="view-link">View rooms <i data-lucide="arrow-right"></i></span>
        </div>
      </div>
    </article>`;
}

function skeletons(n) {
  return Array.from({ length: n }, () => `<div class="stay skeleton"><div class="stay-img"></div><div class="stay-body"><div class="sk-line w60"></div><div class="sk-line w40"></div><div class="sk-line"></div></div></div>`).join("");
}
