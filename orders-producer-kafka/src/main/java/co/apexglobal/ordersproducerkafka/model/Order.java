package co.apexglobal.ordersproducerkafka.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    private String orderId;
    private String customerId;
    private List<Product> products;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Product {
        private String productId;
        private String name;
        private double price;
    }
}
