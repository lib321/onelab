package com.onelab.microservices;

import com.onelab.microservices.model.Order;
import com.onelab.microservices.model.OrderItem;
import com.onelab.microservices.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@DataJpaTest
public class OrderRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    @Autowired
    OrderRepository orderRepository;

    @Test
    void connectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @BeforeEach
    void setUp() {
        Order order = new Order(null, "CustomerTest", new ArrayList<>());
        Order order1 = new Order(null, "CustomerTest", new ArrayList<>());
        List<OrderItem> items = List.of(new OrderItem(null, 1L, "ProductA", 2, order));
        order.setItems(items);
        List<OrderItem> items1 = List.of(
                new OrderItem(null, 2L, "ProductB", 1, order1),
                new OrderItem(null, 3L, "ProductC", 3, order1));
        order1.setItems(items1);
        orderRepository.save(order);
        orderRepository.save(order1);
    }

    @Test
    @Transactional
    void shouldReturnProductByName() {
        List<Order> orders = orderRepository.findByCustomerName("CustomerTest");
        assertThat(orders).isNotNull();
        assertThat(orders.size()).isEqualTo(2);
        assertThat(orders.get(0).getCustomerName()).isEqualTo("CustomerTest");
        assertThat(orders.get(1).getCustomerName()).isEqualTo("CustomerTest");
        assertThat(orders.get(0).getItems().get(0).getProductName()).isEqualTo("ProductA");
        assertThat(orders.get(1).getItems().get(0).getProductName()).isEqualTo("ProductB");
        assertThat(orders.get(1).getItems().get(1).getProductName()).isEqualTo("ProductC");
    }
}
