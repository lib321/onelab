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
import java.util.Optional;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(App.class, args);

        UserServiceImpl userService = applicationContext.getBean(UserServiceImpl.class);
        OrderServiceImpl orderService = applicationContext.getBean(OrderServiceImpl.class);

        Users newUser = Users.builder()
                .login("login4")
                .password("pass4")
                .firstname("name4")
                .lastname("surname4")
                .build();
        userService.save(newUser);

        userService.save(null);

        List<OrderTotalDTO> orderTotalDTOList = orderService.getTotalProductsPerOrderByUserId(1);
        for (OrderTotalDTO totalDTO : orderTotalDTOList) {
            System.out.println("order id: " + totalDTO.getOrderId() + " total: " + totalDTO.getTotalCount());
        }
    }
}
