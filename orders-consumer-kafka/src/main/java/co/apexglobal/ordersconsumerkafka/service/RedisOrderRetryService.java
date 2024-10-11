package co.apexglobal.ordersconsumerkafka.service;

import co.apexglobal.ordersconsumerkafka.model.Order;
import co.apexglobal.ordersconsumerkafka.model.OrderFailed;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RedisOrderRetryService {

    private final OrderManagementService orderManagementService;
    private final KafkaTemplate<String, OrderFailed> kafkaTemplate;
    private final String kafkaTopicRetry = "orders-retry";
    private final RedisService redisService;


    public RedisOrderRetryService(OrderManagementService orderManagementService, KafkaTemplate<String, OrderFailed> kafkaTemplate, RedisService redisService) {
        this.orderManagementService = orderManagementService;
        this.kafkaTemplate = kafkaTemplate;
        this.redisService = redisService;
    }

    @Retryable(
            value = {FeignException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void processOrderRetry(Order order) {
        int retryCount = redisService.incrementRetryCount(order.getOrderId());
        redisService.saveOrder(order.getOrderId(), order);
        log.info("Retry attempt: {}", retryCount);
        orderManagementService.processOrder(order);
    }

    @Recover
    public void recover(Exception e, Order order) {
        int retryCount = redisService.incrementRetryCount(order.getOrderId());
        OrderFailed failedOrder = OrderFailed.builder()
                .order(order)
                .retryCount(retryCount)
                .errorMessage("Failed after " + retryCount + " retries: " + e.getMessage())
                .build();
        log.info("Send kafka topic: {}", order);
        kafkaTemplate.send(kafkaTopicRetry, failedOrder);
    }




}