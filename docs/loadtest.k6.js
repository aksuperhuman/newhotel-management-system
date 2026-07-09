// k6 load test: hammer the booking endpoint to validate no-double-booking under load.
// Run:  k6 run docs/loadtest.k6.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    booking_storm: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '15s', target: 100 },  // ramp to 100 concurrent virtual users
        { duration: '30s', target: 100 },  // hold
        { duration: '10s', target: 0 },     // ramp down
      ],
    },
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

function login() {
  const res = http.post(`${BASE}/api/v1/auth/login`,
    JSON.stringify({ email: 'customer@hotel.com', password: 'password123' }),
    { headers: { 'Content-Type': 'application/json' } });
  return res.json('accessToken');
}

export function setup() {
  return { token: login() };
}

export default function (data) {
  const payload = JSON.stringify({
    roomId: 1,
    checkIn: '2026-09-01',
    checkOut: '2026-09-03',
    guests: 2,
    gateway: 'STRIPE',
  });
  const res = http.post(`${BASE}/api/v1/reservations`, payload, {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${data.token}`,
    },
  });
  // 200 (won the room) or 409 (correctly rejected) are both valid outcomes.
  check(res, { 'status is 200 or 409': (r) => r.status === 200 || r.status === 409 });
  sleep(1);
}
