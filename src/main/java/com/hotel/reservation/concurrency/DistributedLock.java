package com.hotel.reservation.concurrency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/**
 * Redis-based distributed lock (Redlock-lite, single node).
 *
 * WHY: JPA optimistic/pessimistic locks only serialize access WITHIN one
 * database. When the app runs as multiple horizontally-scaled instances, we
 * still want to cheaply reject obviously-competing booking attempts before
 * they ever hit the DB. A short-lived Redis lock keyed by room+date-range
 * acts as a fast, cross-instance gate that absorbs the thundering herd.
 *
 * Acquire = SET key token NX PX ttl (atomic).
 * Release = Lua compare-and-delete so we only delete OUR token, never one a
 * later holder acquired after our TTL expired.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {

    private final RedisTemplate<String, String> stringRedisTemplate;

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('del', KEYS[1]) " +
            "else return 0 end", Long.class);

    /**
     * @return a lock token if acquired, or null if the lock is held elsewhere.
     */
    public String tryAcquire(String key, Duration ttl) {
        String token = UUID.randomUUID().toString();
        Boolean ok = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, token, ttl);
        if (Boolean.TRUE.equals(ok)) {
            log.debug("Acquired distributed lock {}", key);
            return token;
        }
        return null;
    }

    public void release(String key, String token) {
        if (token == null) return;
        stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token);
        log.debug("Released distributed lock {}", key);
    }

    public static String roomLockKey(Long roomId) {
        return "lock:room:" + roomId;
    }
}
