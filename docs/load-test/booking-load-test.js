// k6 load test: sustained booking traffic against the reservation engine.
// Run: k6 run docs/load-test/booking-load-test.js
// Prereq: seed a CUSTOMER token into TOKEN below.
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    booking_burst: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 200 },
        { duration: '1m',  target: 500 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<800'],
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const TOKEN = __ENV.TOKEN || '';

export default function () {
  const payload = JSON.stringify({
    roomId: 2,
    checkIn: '2026-09-10',
    checkOut: '2026-09-12',
    guests: 2,
    paymentProvider: 'STRIPE',
  });
  const res = http.post(`${BASE}/api/v1/reservations`, payload, {
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${TOKEN}` },
  });
  // 201 = won the room, 409 = correctly rejected (no double-booking)
  check(res, { 'status is 201 or 409': (r) => r.status === 201 || r.status === 409 });
  sleep(1);
}
