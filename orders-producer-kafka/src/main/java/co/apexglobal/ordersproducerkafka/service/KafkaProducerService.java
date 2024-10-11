package co.apexglobal.ordersproducerkafka.service;

import co.apexglobal.ordersproducerkafka.model.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Order> kafkaTemplate;

    @Value("${kafka.topic.name}")
    private String topicName;

    public KafkaProducerService(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrder(Order order) {
        kafkaTemplate.send(topicName, order);
    }
}