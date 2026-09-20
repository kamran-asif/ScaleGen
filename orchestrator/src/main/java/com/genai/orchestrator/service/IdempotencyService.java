package com.genai.orchestrator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public IdempotencyService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateFingerprint(String userId, String prompt, String model) {
        try {
            String raw = String.format("%s:%s:%s", userId != null ? userId : "", prompt != null ? prompt : "", model != null ? model : "");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(prompt.hashCode());
        }
    }

    public Object getCachedResponse(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        return redisTemplate.opsForValue().get("idempotency:" + idempotencyKey);
    }

    public void cacheResponse(String idempotencyKey, Object response) {
        if (idempotencyKey != null && !idempotencyKey.isBlank() && response != null) {
            redisTemplate.opsForValue().set("idempotency:" + idempotencyKey, response, 24, java.util.concurrent.TimeUnit.HOURS);
            log.info("[Idempotency] Cached response for key: {}", idempotencyKey);
        }
    }
}
