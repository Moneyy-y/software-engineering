package com.catering.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 验证码存储：优先 Redis；Redis 不可用时降级到进程内缓存（便于本地开发）。
 */
@Component
public class CaptchaStore {

    private static final Logger log = LoggerFactory.getLogger(CaptchaStore.class);
    private static final String KEY_PREFIX = "captcha:";

    private final StringRedisTemplate redisTemplate;
    private final Map<String, Entry> memory = new ConcurrentHashMap<>();
    private volatile boolean redisAvailable = true;

    public CaptchaStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String captchaKey, String code, long ttlMinutes) {
        String redisKey = KEY_PREFIX + captchaKey;
        if (redisAvailable) {
            try {
                redisTemplate.opsForValue().set(redisKey, code, ttlMinutes, TimeUnit.MINUTES);
                return;
            } catch (Exception e) {
                redisAvailable = false;
                log.warn("Redis 不可用，验证码改用内存缓存: {}", e.getMessage());
            }
        }
        memory.put(captchaKey, new Entry(code, System.currentTimeMillis() + ttlMinutes * 60_000L));
    }

    public String get(String captchaKey) {
        if (redisAvailable) {
            try {
                return redisTemplate.opsForValue().get(KEY_PREFIX + captchaKey);
            } catch (Exception e) {
                redisAvailable = false;
                log.warn("Redis 读取失败，尝试内存缓存: {}", e.getMessage());
            }
        }
        Entry entry = memory.get(captchaKey);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() > entry.expireAt) {
            memory.remove(captchaKey);
            return null;
        }
        return entry.code;
    }

    public void delete(String captchaKey) {
        if (redisAvailable) {
            try {
                redisTemplate.delete(KEY_PREFIX + captchaKey);
                return;
            } catch (Exception e) {
                redisAvailable = false;
            }
        }
        memory.remove(captchaKey);
    }

    private static final class Entry {
        final String code;
        final long expireAt;

        Entry(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }
}
