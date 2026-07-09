import { api } from "../api.js";
import { setTokens } from "../auth.js";
import { navigate } from "../router.js";
import { toast } from "../ui.js";

export async function loginView(app) {
  app.innerHTML = `
    <section class="auth-shell">
      <div class="auth-card">
        <div class="auth-tabs">
          <button class="auth-tab on" data-tab="login">Sign in</button>
          <button class="auth-tab" data-tab="register">Create account</button>
        </div>

        <form id="login-form" class="auth-form">
          <h2>Welcome back</h2>
          <label>Email<input type="email" name="email" value="customer@hotel.com" required /></label>
          <label>Password<input type="password" name="password" value="password123" required /></label>
          <button class="btn-primary lg" type="submit">Sign in</button>
          <p class="auth-hint">Seed accounts: admin@ / manager@ / customer@hotel.com · password123</p>
        </form>

        <form id="register-form" class="auth-form hidden">
          <h2>Create your account</h2>
          <label>Full name<input name="fullName" required /></label>
          <label>Email<input type="email" name="email" required /></label>
          <label>Password<input type="password" name="password" minlength="8" required /></label>
          <label>Role
            <select name="role">
              <option value="CUSTOMER">Customer</option>
              <option value="HOTEL_MANAGER">Hotel manager</option>
              <option value="ADMIN">Admin</option>
            </select>
          </label>
          <button class="btn-primary lg" type="submit">Create account</button>
        </form>
      </div>
      <aside class="auth-art"><div class="auth-art-inner">
        <h1>Rooms that hold the night <em>just for you</em>.</h1>
        <p>Real-time availability, no double bookings.</p>
      </div></aside>
    </section>`;

  const login = app.querySelector("#login-form");
  const register = app.querySelector("#register-form");
  app.querySelectorAll(".auth-tab").forEach((t) =>
    t.addEventListener("click", () => {
      app.querySelectorAll(".auth-tab").forEach((x) => x.classList.remove("on"));
      t.classList.add("on");
      const isLogin = t.dataset.tab === "login";
      login.classList.toggle("hidden", !isLogin);
      register.classList.toggle("hidden", isLogin);
    })
  );

  login.addEventListener("submit", async (e) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(login));
    try {
      setTokens(await api.login(data));
      toast("Signed in", "success");
      navigate("/");
    } catch (err) {
      toast(err.message, "error");
    }
  });

  register.addEventListener("submit", async (e) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(register));
    try {
      setTokens(await api.register(data));
      toast("Account created", "success");
      navigate("/");
    } catch (err) {
      toast(err.message, "error");
    }
  });
}
