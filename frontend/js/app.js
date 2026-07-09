// App bootstrap: register routes + guards, wire the topbar to auth changes, go.
import { route, startRouter } from "./router.js";
import { renderTopbar } from "./ui.js";
import { onAuthChange } from "./auth.js";
import { loginView } from "./views/login.js";
import { searchView } from "./views/search.js";
import { hotelView } from "./views/hotel.js";
import { tripsView } from "./views/trips.js";
import { adminView } from "./views/admin.js";

const requireAuth = ({ authed }) => authed || "/login";
const requireStaff = ({ authed, hasRole }) =>
  !authed ? "/login" : hasRole("ADMIN", "HOTEL_MANAGER") || "/";

route("/", searchView);
route("/login", loginView);
route("/hotels/:id", hotelView);
route("/trips", tripsView, requireAuth);
route("/admin", adminView, requireStaff);

renderTopbar();
onAuthChange(renderTopbar);
startRouter();
