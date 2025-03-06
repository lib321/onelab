package org.onelab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onelab.model.OrderProducts;
import org.onelab.model.Orders;
import org.onelab.model.Product;
import org.onelab.model.Users;
import org.onelab.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EntityRelationShipTest {

    @Autowired
    private UserRepo userRepo;

    @Test
    public void testUserOrderRelation() {
        Users user = Users.builder()
                .login("login1")
                .password("pass1")
                .firstname("name1")
                .lastname("surname1")
                .orders(new ArrayList<>())
                .build();

        Product product1 = Product.builder()
                .name("prod1")
                .price(2000)
                .orders(new ArrayList<>())
                .build();

        Product product2 = Product.builder()
                .name("prod2")
                .price(3000)
                .orders(new ArrayList<>())
                .build();

        Orders order1 = Orders.builder()
                .user(user)
                .products(new ArrayList<>())
                .build();

        Orders order2 = Orders.builder()
                .user(user)
                .products(new ArrayList<>())
                .build();

        OrderProducts orderProduct1 = OrderProducts.builder()
                .count(3)
                .product(product1)
                .order(order1)
                .build();

        OrderProducts orderProduct2 = OrderProducts.builder()
                .count(2)
                .product(product2)
                .order(order2)
                .build();

        order1.getProducts().add(orderProduct1);
        order2.getProducts().add(orderProduct2);

        user.getOrders().add(order1);
        user.getOrders().add(order2);

        userRepo.save(user);

        Users savedUser = userRepo.findById(user.getId()).orElseThrow();
        assertThat(savedUser.getOrders()).hasSize(2);
        assertThat(savedUser.getOrders().get(0).getProducts().get(0).getProduct().getName()).isEqualTo("prod1");
        assertThat(savedUser.getOrders().get(1).getProducts().get(0).getProduct().getName()).isEqualTo("prod2");
    }
}
