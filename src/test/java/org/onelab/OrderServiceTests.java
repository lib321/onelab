package org.onelab;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onelab.dto.OrderTotalDTO;
import org.onelab.model.OrderProducts;
import org.onelab.model.Orders;
import org.onelab.model.Product;
import org.onelab.model.Users;
import org.onelab.repository.OrderProductRepo;
import org.onelab.repository.OrderRepo;
import org.onelab.serviceImpl.OrderServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTests {

    @Mock
    private OrderRepo orderRepo;
    @Mock
    private OrderProductRepo orderProductRepo;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Orders order, order1;
    private Users user;
    private Product product, product1;
    private OrderProducts orderProduct, orderProduct1;

    @BeforeEach
    public void setUp() {
        user = Users.builder()
                .id(1)
                .login("login1")
                .password("pass1")
                .firstname("name1")
                .lastname("surname1")
                .build();

        product = Product.builder()
                .id(1)
                .name("prod1")
                .price(2000)
                .orders(new ArrayList<>())
                .build();

        order = Orders.builder()
                .id(1)
                .user(user)
                .products(new ArrayList<>())
                .build();

        orderProduct = OrderProducts.builder()
                .id(1)
                .count(3)
                .product(product)
                .order(order)
                .build();

        order.getProducts().add(orderProduct);
        product.getOrders().add(orderProduct);

        product1 = Product.builder()
                .id(2)
                .name("prod2")
                .price(3000)
                .orders(new ArrayList<>())
                .build();

        order1 = Orders.builder()
                .id(2)
                .user(user)
                .products(new ArrayList<>())
                .build();

        orderProduct1 = OrderProducts.builder()
                .id(2)
                .count(2)
                .product(product1)
                .order(order1)
                .build();

        order1.getProducts().add(orderProduct1);
        product1.getOrders().add(orderProduct1);
    }

    @DisplayName("Тест метода save()")
    @Test
    public void givenOrderObject_whenSaveOrder_thenReturnOrderObject() {
        when(orderRepo.save(order)).thenReturn(order);

        Orders savedOrder = orderService.save(order);
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getUser().getFirstname()).isEqualTo("name1");
    }

    @DisplayName("Тест метода getOrders()")
    @Test
    public void givenOrderList_whenGetOrders_thenReturnOrderList() {
        when(orderRepo.findAll()).thenReturn(List.of(order, order1));

        List<Orders> ordersList = orderService.getOrders();
        assertThat(ordersList).isNotNull();
        assertThat(ordersList.size()).isEqualTo(2);
    }

    @DisplayName("Тест метода getOrderById(int orderId)")
    @Test
    public void givenOrderId_whenGetOrderById_thenReturnOrderObject() {
        when(orderRepo.findById(1)).thenReturn(Optional.of(order));

        Optional<Orders> foundOrder = orderService.getOrderById(1);
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.get().getUser().getFirstname()).isEqualTo("name1");
    }

    @DisplayName("Тест метода deleteById(int orderId)")
    @Test
    public void givenOrderId_whenDeleteById_thenNothing() {
        orderService.deleteById(1);

        verify(orderRepo).deleteById(1);
    }

    @DisplayName("Тест метода findOrdersByUserId(int userId)")
    @Test
    public void givenOrderListByUserId_whenFindOrdersByUserId_thenReturnOrderList() {
        when(orderRepo.findByUserId(1)).thenReturn(List.of(order, order1));

        List<Orders> ordersList = orderService.findOrdersByUserId(1);
        assertThat(ordersList).isNotNull();
        assertThat(ordersList.size()).isEqualTo(2);
    }

    @DisplayName("Тест метода getTotalProductsPerOrderByUserId(int userId)")
    @Test
    public void givenUserId_whenGetTotalProductsPerOrderByUserId_thenReturnOrderTotalDTOList() {
        List<OrderTotalDTO> expectedDTOs = List.of(
                new OrderTotalDTO(1, 3),
                new OrderTotalDTO(2, 2)
        );

        when(orderProductRepo.getTotalProductsPerOrderByUserId(1)).thenReturn(expectedDTOs);

        List<OrderTotalDTO> actualDTOs = orderService.getTotalProductsPerOrderByUserId(1);

        assertThat(actualDTOs).isNotNull();
        assertThat(actualDTOs).isEqualTo(expectedDTOs);
        assertThat(actualDTOs.size()).isEqualTo(2);

        verify(orderProductRepo).getTotalProductsPerOrderByUserId(1);
    }
}
