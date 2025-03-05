package org.onelab;

import org.onelab.dto.OrderTotalDTO;
import org.onelab.model.OrderProducts;
import org.onelab.model.Orders;
import org.onelab.model.Users;
import org.onelab.serviceImpl.OrderServiceImpl;
import org.onelab.serviceImpl.UserServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.List;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(App.class, args);

        OrderServiceImpl orderService = applicationContext.getBean(OrderServiceImpl.class);

        for (OrderTotalDTO orderTotalDTO : orderService.getTotalProductsPerOrderByUserId(1)) {
            System.out.println("order id: " + orderTotalDTO.getOrderId() + " count: " + orderTotalDTO.getTotalCount());
        }
    }
}
