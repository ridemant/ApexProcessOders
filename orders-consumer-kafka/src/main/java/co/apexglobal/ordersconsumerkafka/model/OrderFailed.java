package co.apexglobal.ordersconsumerkafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderFailed {
    private Order order;
    private String errorMessage;
    private int retryCount;
}
