package com.lightfood.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis-backed menu cache with a small in-process fallback.
 *
 * Redis is the primary cache. If Redis is unavailable, the application still
 * works and falls back to process-local caching, so local development does not
 * require a running Redis instance.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MenuCache {

    public static final String CATEGORIES_KEY = "lightfood:menu:categories:v1";
    public static final String HOT_DISHES_KEY = "lightfood:menu:hot-dishes:v1";

    private static final Duration CATEGORIES_TTL = Duration.ofMinutes(30);
    private static final Duration HOT_DISHES_TTL = Duration.ofMinutes(10);
    private static final long REDIS_RETRY_MILLIS = 60_000L;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${lightfood.cache.redis-enabled:true}")
    private boolean redisEnabled;

    private final Map<String, CacheEntry> fallback = new ConcurrentHashMap<>();
    private volatile long redisRetryAt = 0L;

    public <T> T get(String key, TypeReference<T> type) {
        String raw = getRaw(key);
        if (raw == null) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, type);
        } catch (Exception ex) {
            log.warn("Failed to read menu cache {}: {}", key, ex.getMessage());
            return null;
        }
    }

    public void putCategories(Object value) {
        put(CATEGORIES_KEY, value, CATEGORIES_TTL);
    }

    public void putHotDishes(Object value) {
        put(HOT_DISHES_KEY, value, HOT_DISHES_TTL);
    }

    public void evictAll() {
        fallback.keySet().removeIf(key -> key.startsWith("lightfood:menu:"));
        if (!redisEnabled || isRedisDown()) {
            return;
        }
        try {
            redisTemplate.delete(List.of(CATEGORIES_KEY, HOT_DISHES_KEY));
        } catch (Exception ex) {
            markRedisDown(ex);
        }
    }

    private void put(String key, Object value, Duration ttl) {
        try {
            String raw = objectMapper.writeValueAsString(value);
            fallback.put(key, new CacheEntry(raw, System.currentTimeMillis() + ttl.toMillis()));
            if (redisEnabled && !isRedisDown()) {
                redisTemplate.opsForValue().set(key, raw, ttl);
            }
        } catch (Exception ex) {
            log.warn("Failed to write menu cache {}: {}", key, ex.getMessage());
            markRedisDown(ex);
        }
    }

    private String getRaw(String key) {
        if (!redisEnabled) {
            return fallbackGet(key);
        }
        if (isRedisDown()) {
            return fallbackGet(key);
        }
        try {
            String raw = redisTemplate.opsForValue().get(key);
            if (raw != null) {
                return raw;
            }
            return null;
        } catch (Exception ex) {
            markRedisDown(ex);
            return fallbackGet(key);
        }
    }

    private String fallbackGet(String key) {
        CacheEntry entry = fallback.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAt() < System.currentTimeMillis()) {
            fallback.remove(key);
            return null;
        }
        return entry.json();
    }

    private boolean isRedisDown() {
        return System.currentTimeMillis() < redisRetryAt;
    }

    private void markRedisDown(Exception ex) {
        boolean firstFailure = !isRedisDown();
        redisRetryAt = System.currentTimeMillis() + REDIS_RETRY_MILLIS;
        if (firstFailure) {
            log.warn("Redis menu cache is unavailable, using local fallback for {}s: {}",
                    REDIS_RETRY_MILLIS / 1000, ex.getMessage());
        }
    }

    private record CacheEntry(String json, long expiresAt) {
    }
}
