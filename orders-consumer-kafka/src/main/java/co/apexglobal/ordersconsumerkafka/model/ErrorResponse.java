package co.apexglobal.ordersconsumerkafka.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String status;
    private int code;
    private String message;
}
