package com.hotelreservation.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Redis-based distributed lock (the Redlock-lite pattern for a single node).
 *
 * WHY: JVM locks and DB row locks only protect a single application instance /
 * transaction. When the service is horizontally scaled behind a load balancer,
 * two pods can process bookings for the same room concurrently. A lock in Redis
 * (shared by all pods) gives us a cluster-wide mutex BEFORE we ever hit the DB,
 * shedding contention early and keeping DB transactions short.
 *
 * Correctness details:
 *  - SET key value NX PX ttl  -> atomic "acquire only if absent" with auto-expiry
 *    so a crashed holder can't deadlock the room forever.
 *  - A unique token per acquisition + a Lua compare-and-delete release ensures we
 *    only ever delete OUR lock, never one a later owner acquired after our TTL.
 */
@Slf4j
@Service
public class DistributedLockService {

    private static final String RELEASE_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<Long> releaseScript;

    public DistributedLockService(StringRedisTemplate redis) {
        this.redis = redis;
        this.releaseScript = new DefaultRedisScript<>(RELEASE_SCRIPT, Long.class);
    }

    /** Acquire the lock; returns a release token on success, or null if not acquired. */
    public String tryLock(String key, Duration ttl) {
        String token = UUID.randomUUID().toString();
        Boolean ok = redis.opsForValue().setIfAbsent(key, token, ttl);
        if (Boolean.TRUE.equals(ok)) {
            log.debug("Acquired distributed lock {} (token={})", key, token);
            return token;
        }
        return null;
    }

    /** Release only if we still own the lock (compare-and-delete, atomic in Redis). */
    public void unlock(String key, String token) {
        if (token == null) return;
        Long released = redis.execute(releaseScript, List.of(key), token);
        log.debug("Released distributed lock {} -> {}", key, released);
    }

    public static String roomLockKey(Long roomId) {
        return "lock:room:" + roomId;
    }
}
