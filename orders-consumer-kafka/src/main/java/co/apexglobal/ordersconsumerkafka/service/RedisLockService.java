package co.apexglobal.ordersconsumerkafka.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisLockService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String LOCK_PREFIX = "order:lock:";
    private static final long LOCK_EXPIRE_TIME = 10_000;

    public RedisLockService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Obtener el lock
    public boolean tryLock(String orderId) {
        String key = LOCK_PREFIX + orderId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "locked", LOCK_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        return success != null && success;
    }

    // Libera el lock
    public void unlock(String orderId) {
        String key = LOCK_PREFIX + orderId;
        redisTemplate.delete(key); // Elimina el lock
    }
}