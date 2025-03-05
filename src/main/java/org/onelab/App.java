package org.onelab;

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

        UserServiceImpl userService = applicationContext.getBean(UserServiceImpl.class);
        OrderServiceImpl orderService = applicationContext.getBean(OrderServiceImpl.class);

        List<Orders> orders = orderService.findOrdersByUserId(1);
        for (Orders order : orders) {
            System.out.println(order.getId());
        }
    }
}
