package co.apexglobal.ordersconsumerkafka;

import co.apexglobal.ordersconsumerkafka.client.ExternalOrderClient;
import co.apexglobal.ordersconsumerkafka.consumer.OrderConsumerService;
import co.apexglobal.ordersconsumerkafka.model.Customer;
import co.apexglobal.ordersconsumerkafka.model.Order;
import co.apexglobal.ordersconsumerkafka.model.OrderFailed;
import co.apexglobal.ordersconsumerkafka.service.OrderManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
class OrderConsumerServiceTest {


    @Mock
    private ExternalOrderClient externalOrderClient;

    @Mock
    private OrderManagementService orderManagementService;

    @Mock
    private KafkaTemplate<String, OrderFailed> kafkaTemplate;

    @InjectMocks
    private OrderConsumerService orderConsumerService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderManagementService = new OrderManagementService(externalOrderClient, kafkaTemplate);


    }




    @Test
    void testProcessOrder_whenOrderIsValid() throws Exception {
        Order order = new Order();
        order.setOrderId("order-" + UUID.randomUUID());
        order.setCustomerId("customer-1");

        Order.Product product1 = new Order.Product();
        product1.setProductId("product-1");
        Order.Product product2 = new Order.Product();
        product2.setProductId("product-2");
        order.setProducts(Arrays.asList(product1, product2));

        Customer validCustomer = new Customer();
        validCustomer.setStatus("active");
        when(externalOrderClient.getCustomer("customer-1")).thenReturn(new ResponseEntity<>(validCustomer, HttpStatus.OK));

        Order.Product enrichedProduct1 = new Order.Product();
        enrichedProduct1.setProductId("product-1");
        Order.Product enrichedProduct2 = new Order.Product();
        enrichedProduct2.setProductId("product-2");
        when(externalOrderClient.getProduct("product-1")).thenReturn(new ResponseEntity<>(enrichedProduct1, HttpStatus.OK));
        when(externalOrderClient.getProduct("product-2")).thenReturn(new ResponseEntity<>(enrichedProduct2, HttpStatus.OK));

        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("code", HttpStatus.CREATED.value());
        when(externalOrderClient.createOrder(order)).thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.CREATED));

        boolean result = orderManagementService.processOrder(order);

        assertTrue(result);
    }


    @Test
    void testProcessOrder_whenCustomerStatusIsInactive() throws Exception {
        Order order = new Order();
        order.setOrderId("order-" + UUID.randomUUID());
        order.setCustomerId("customer-1");

        Order.Product product1 = new Order.Product();
        product1.setProductId("product-1");
        Order.Product product2 = new Order.Product();
        product2.setProductId("product-2");
        order.setProducts(Arrays.asList(product1, product2));

        // Simular la respuesta de un cliente con estado "inactive"
        Customer inactiveCustomer = new Customer();
        inactiveCustomer.setStatus("inactive");
        when(externalOrderClient.getCustomer("customer-1"))
                .thenReturn(new ResponseEntity<>(inactiveCustomer, HttpStatus.OK));

        // Ejecutar la prueba
        boolean result = orderManagementService.processOrder(order);

        // Verificar que la orden no se procesa porque el cliente está inactivo
        assertFalse(result);
    }

    @Test
    void testProcessOrder_whenCustomerDoesNotExist() throws Exception {
        // Crear una orden válida
        Order order = new Order();
        order.setOrderId("order-" + UUID.randomUUID());
        order.setCustomerId("customer-1");

        Order.Product product1 = new Order.Product();
        product1.setProductId("product-1");
        Order.Product product2 = new Order.Product();
        product2.setProductId("product-2");
        order.setProducts(Arrays.asList(product1, product2));

        // Simular que el customer no existe (devolver una excepción Feign NotFound)
        Request request = Request.create(Request.HttpMethod.GET, "/customers/customer-1", Map.of(), new byte[0], null);
        when(externalOrderClient.getCustomer("customer-1"))
                .thenThrow(FeignException.errorStatus("GET", Response.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .request(request)
                        .build()));

        boolean result = orderManagementService.processOrder(order);

        assertFalse(result);
    }
    @Test
    void testProcessOrder_whenProductDoesNotExist() throws Exception {
        // Crear una orden válida
        Order order = new Order();
        order.setOrderId("order-" + UUID.randomUUID());
        order.setCustomerId("customer-1");

        Order.Product product1 = new Order.Product();
        product1.setProductId("product-1");
        Order.Product product2 = new Order.Product();
        product2.setProductId("product-2");
        order.setProducts(Arrays.asList(product1, product2));

        // cliente válido con estado "active"
        Customer validCustomer = new Customer();
        validCustomer.setStatus("active");
        when(externalOrderClient.getCustomer("customer-1"))
                .thenReturn(new ResponseEntity<>(validCustomer, HttpStatus.OK));

        // el primer producto existe
        Order.Product enrichedProduct1 = new Order.Product();
        enrichedProduct1.setProductId("product-1");
        when(externalOrderClient.getProduct("product-1"))
                .thenReturn(new ResponseEntity<>(enrichedProduct1, HttpStatus.OK));

        Request request = Request.create(Request.HttpMethod.GET, "/products/product-2", Map.of(), new byte[0], null);
        when(externalOrderClient.getProduct("product-2"))
                .thenThrow(FeignException.errorStatus("GET", Response.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .request(request)
                        .build()));

        boolean result = orderManagementService.processOrder(order);

        assertFalse(result);
    }

}
