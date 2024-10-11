package co.apexglobal.ordersconsumerkafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableFeignClients
@ImportAutoConfiguration({FeignAutoConfiguration.class})
@EnableRetry
@EnableAspectJAutoProxy
public class OrdersConsumerKafkaApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrdersConsumerKafkaApplication.class, args);
    }
}
