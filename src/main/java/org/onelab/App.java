package org.onelab;

import org.onelab.config.AppConfig;
import org.onelab.model.OrderProducts;
import org.onelab.model.Orders;
import org.onelab.model.Product;
import org.onelab.model.Users;
import org.onelab.repoimpl.OrderRepoImpl;
import org.onelab.repoimpl.UserRepoImpl;
import org.onelab.service.AppService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {

        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        Users user1 = Users.builder()
                .id(4)
                .firstname("name4")
                .lastname("surname4")
                .build();

        Orders order1 = Orders.builder()
                .id(4)
                .userId(4)
                .build();

        AppService appService = context.getBean(AppService.class);

        appService.saveUser(user1);
        appService.findAllUsers();
        appService.findAllOrders();
        appService.findUserById(1);
        appService.saveOrder(order1);
        appService.findUserById(10);
    }
}
