package com.hotel.reservation;

import com.hotel.reservation.concurrency.DistributedLock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class DistributedLockTest {

    @Test
    void acquireReturnsTokenWhenKeyIsFree() {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, String> template = Mockito.mock(RedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops = Mockito.mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(eq("lock:room:1"), any(), any(Duration.class))).thenReturn(true);

        DistributedLock lock = new DistributedLock(template);
        String token = lock.tryAcquire("lock:room:1", Duration.ofSeconds(30));

        assertThat(token).isNotNull();
    }

    @Test
    void acquireReturnsNullWhenKeyHeld() {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, String> template = Mockito.mock(RedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops = Mockito.mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(false);

        DistributedLock lock = new DistributedLock(template);
        assertThat(lock.tryAcquire("lock:room:1", Duration.ofSeconds(30))).isNull();
    }
}
