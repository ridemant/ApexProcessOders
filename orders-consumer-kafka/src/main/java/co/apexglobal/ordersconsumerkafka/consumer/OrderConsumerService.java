package co.apexglobal.ordersconsumerkafka.consumer;

import co.apexglobal.ordersconsumerkafka.model.Order;
import co.apexglobal.ordersconsumerkafka.service.OrderManagementService;
import co.apexglobal.ordersconsumerkafka.service.RedisLockService;
import co.apexglobal.ordersconsumerkafka.service.RedisOrderRetryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderConsumerService {


    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderManagementService orderManagementService;
    private final RedisOrderRetryService orderRetryService;
    private final RedisLockService redisLockService;


    public OrderConsumerService(OrderManagementService orderManagementService, RedisOrderRetryService orderRetryService, RedisLockService redisLockService) {
        this.orderManagementService = orderManagementService;
        this.orderRetryService = orderRetryService;
        this.redisLockService = redisLockService;
    }

    // Orders topico
    @KafkaListener(topics = "orders", groupId = "order-group")
    public void consume(String order) throws Exception {
        Order objOrder = objectMapper.readValue(order, Order.class);
        String orderId = objOrder.getOrderId();

        // 1. Intentar obtener el lock para el pedido actual
        if (redisLockService.tryLock(orderId)) {
            try {
                // 2. Procesar la orden normalmente
                orderManagementService.processOrder(objOrder);
            } catch (Exception e) {
                // 3. Mecanismo de reintentos exponenciales (5 intentos cada 2 seg a un potencia de 2)
                orderRetryService.processOrderRetry(objOrder);
            } finally {
                // 4. Liberar el lock
                redisLockService.unlock(orderId);
            }
        } else {
            log.warn("Order {} is already being processed by another worker.", orderId);
        }
    }
}
