package co.apexglobal.ordersconsumerkafka.service;

import co.apexglobal.ordersconsumerkafka.model.Order;
import co.apexglobal.ordersconsumerkafka.model.OrderFailed;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;



@Service
@Slf4j
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String RETRY_COUNT_KEY_PREFIX = "order:retry:";
    private static final String ORDER_KEY_PREFIX = "order:data:";

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public int incrementRetryCount(String orderId) {
        String key = RETRY_COUNT_KEY_PREFIX + orderId;
        Integer retryCount = (Integer) redisTemplate.opsForValue().get(key);

        if (retryCount == null) retryCount = 0;
        retryCount++;

        redisTemplate.opsForValue().set(key, retryCount);
        return retryCount;
    }

    public void saveOrder(String orderId, Order order) {
        String orderKey = ORDER_KEY_PREFIX + orderId;
        redisTemplate.opsForValue().set(orderKey, order);
    }

}
