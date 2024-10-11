package co.apexglobal.ordersconsumerkafka.service;

import co.apexglobal.ordersconsumerkafka.client.ExternalOrderClient;
import co.apexglobal.ordersconsumerkafka.model.Customer;
import co.apexglobal.ordersconsumerkafka.model.Order;
import co.apexglobal.ordersconsumerkafka.model.OrderFailed;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

import org.springframework.http.ResponseEntity;


@Service
@Slf4j
public class OrderManagementService {


    private final ExternalOrderClient externalOrderClient;

    private final KafkaTemplate<String, OrderFailed> kafkaTemplate;
    private final String kafkaTopicFailed = "orders-failed";


    public OrderManagementService(ExternalOrderClient externalOrderClient, KafkaTemplate<String, OrderFailed> kafkaTemplate) {
        this.externalOrderClient = externalOrderClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public boolean processOrder(Order order) {
        log.info("Processing order obj: {}", order);
        log.info("Processing order: {}", order.getOrderId());
        System.out.println("Processing order: " + order.getOrderId());

        if (!validateCustomer(order)) {
            return false;
        }

        if (!validateProducts(order)) {
            return false;
        }
        return processOrderCreation(order);
    }

    private boolean validateCustomer(Order order) {
        try {
            ResponseEntity<Customer> customerResponse = externalOrderClient.getCustomer(order.getCustomerId());
            Customer customer = customerResponse.getBody();

            if (customer == null || !"active".equals(customer.getStatus())) {
                sendToKafkaFailed(order, "Customer does not exist or is not active: " + order.getCustomerId());
                return false;
            }
            return true;
        } catch (FeignException e) {
            if (e.status() == 404) {
                sendToKafkaFailed(order, "Customer not found: " + order.getCustomerId());
                return false;
            }
            throw new RuntimeException("Error processing customer validation: " + e.getMessage(), e);
        }
    }

    private boolean validateProducts(Order order) {
        for (Order.Product product : order.getProducts()) {
            try {
                ResponseEntity<Order.Product> productResponse = externalOrderClient.getProduct(product.getProductId());
                Order.Product enrichedProduct = productResponse.getBody();

                if (enrichedProduct == null) {
                    sendToKafkaFailed(order, "Product not found: " + product.getProductId());
                    return false;
                }

                // Enriquecer el producto con más datos
                product.setName(enrichedProduct.getName());
                product.setPrice(enrichedProduct.getPrice());

            } catch (FeignException e) {
                if (e.status() == 404) {
                    sendToKafkaFailed(order, "Product not found: " + product.getProductId());
                    return false;
                }
                throw new RuntimeException("Error processing product validation: " + e.getMessage(), e);
            }
        }
        return true;
    }

    private boolean processOrderCreation(Order order) {
        try {
            Map<String, Object> apiResponse = externalOrderClient.createOrder(order).getBody();
            int responseCode = (int) apiResponse.get("code");

            if (responseCode != HttpStatus.CREATED.value()) {
                sendToKafkaFailed(order, "Order failed with response code: " + responseCode);
                return false;
            }

            return true;
        } catch (FeignException e) {

            if (e.status() == 409) {
                sendToKafkaFailed(order, "Order already exists: " + order.getOrderId());
                return false;
            }
            throw new RuntimeException("Error processing order creation: " + e.getMessage(), e);
        }
    }



    private void sendToKafkaFailed(Order order, String errorMessage) {
        kafkaTemplate.send(kafkaTopicFailed, OrderFailed.builder().order(order)
                .errorMessage(errorMessage).build());
    }

}