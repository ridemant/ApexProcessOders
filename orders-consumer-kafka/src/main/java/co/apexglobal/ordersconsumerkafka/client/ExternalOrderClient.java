package co.apexglobal.ordersconsumerkafka.client;

import co.apexglobal.ordersconsumerkafka.model.Customer;
import co.apexglobal.ordersconsumerkafka.model.Order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@FeignClient(name = "externalOrderClient", url = "${external.api.url}")
public interface ExternalOrderClient {

    // GET para obtener el cliente
    @GetMapping("/customers/{customerId}")
    ResponseEntity<Customer> getCustomer(@PathVariable("customerId") String customerId);

    // GET para obtener los detalles del producto
    @GetMapping("/products/{productId}")
    ResponseEntity<Order.Product> getProduct(@PathVariable("productId") String productId);

    // POST para crear una orden
    @PostMapping("/orders")
    ResponseEntity<Map<String, Object>> createOrder(@RequestBody Order order);
}