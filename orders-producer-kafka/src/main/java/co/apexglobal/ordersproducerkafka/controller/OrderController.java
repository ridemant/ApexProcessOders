package co.apexglobal.ordersproducerkafka.controller;

import co.apexglobal.ordersproducerkafka.common.ApiResponse;
import co.apexglobal.ordersproducerkafka.model.Order;
import co.apexglobal.ordersproducerkafka.service.KafkaProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final KafkaProducerService kafkaProducerService;

    public OrderController(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(@RequestBody Order order) {
        kafkaProducerService.sendOrder(order);
        ApiResponse response = new ApiResponse("success", "Order sent to Kafka successfully");
        return ResponseEntity.ok(response);
    }
}