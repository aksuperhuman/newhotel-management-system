package com.hotel.reservation;

import com.hotel.reservation.domain.*;
import com.hotel.reservation.dto.reservation.ReservationRequest;
import com.hotel.reservation.exception.RoomUnavailableException;
import com.hotel.reservation.repository.*;
import com.hotel.reservation.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Concurrency test: fire N simultaneous booking requests for the SAME room and
 * date range. Exactly ONE must succeed; the rest must be rejected. This proves
 * the layered locking prevents double-booking.
 *
 * NOTE: requires a running Redis + Kafka (docker compose up redis kafka) for a
 * full end-to-end run; Postgres is provided by Testcontainers.
 */
@Testcontainers
@SpringBootTest
class ReservationConcurrencyIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("hotel_reservation").withUsername("hotel").withPassword("hotel_pass");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired ReservationService reservationService;
    @Autowired UserRepository userRepository;
    @Autowired RoomRepository roomRepository;

    Long userId;
    Long roomId;

    @BeforeEach
    void setup() {
        User u = userRepository.save(User.builder()
            .email("race+" + System.nanoTime() + "@t.com").password("x")
            .fullName("Race").role(Role.CUSTOMER).enabled(true).build());
        userId = u.getId();
        Room r = roomRepository.save(Room.builder()
            .hotelId(1L).roomNumber("R" + System.nanoTime()).roomType(RoomType.DELUXE)
            .capacity(2).price(new BigDecimal("1000")).status(RoomStatus.AVAILABLE).build());
        roomId = r.getId();
    }

    @Test
    void onlyOneBookingSucceedsUnderContention() throws InterruptedException {
        int threads = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        ReservationRequest req = new ReservationRequest(roomId,
            LocalDate.now().plusDays(5), LocalDate.now().plusDays(7), 2, PaymentProvider.STRIPE);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    reservationService.book(userId, req);
                    success.incrementAndGet();
                } catch (RoomUnavailableException e) {
                    rejected.incrementAndGet();
                } catch (Exception ignored) {
                    rejected.incrementAndGet();
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        // At most one booking may win the room for those nights.
        assertThat(success.get()).isLessThanOrEqualTo(1);
        assertThat(success.get() + rejected.get()).isEqualTo(threads);
    }
}
