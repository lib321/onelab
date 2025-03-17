package com.onelab.microservices;

import static org.mockito.Mockito.*;

import com.onelab.microservices.dto.OrderDTO;
import com.onelab.microservices.model.Order;
import com.onelab.microservices.model.OrderItem;
import com.onelab.microservices.repository.OrderRepository;
import com.onelab.microservices.service.KafkaLoggingService;
import com.onelab.microservices.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaLoggingService kafkaLoggingService;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrder_savesOrderCorrectly() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setCustomerName("Иван Иванов");
        orderDTO.setProductName("Товар A");
        orderDTO.setQuantity(3);

        orderService.createOrder(orderDTO);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        verify(kafkaLoggingService).log(anyString(), anyString()); 

        Order savedOrder = orderCaptor.getValue();
        assertEquals("Иван Иванов", savedOrder.getCustomerName());
        assertNotNull(savedOrder.getItems());
        assertEquals(1, savedOrder.getItems().size());

        OrderItem savedItem = savedOrder.getItems().get(0);
        assertEquals("Товар A", savedItem.getProductName());
        assertEquals(3, savedItem.getQuantity());
        assertEquals(savedOrder, savedItem.getOrder());
    }

    @Test
    void testCreateOrder_callsRepositoryOnce() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setCustomerName("Иван Иванов");
        orderDTO.setProductName("Товар A");
        orderDTO.setQuantity(3);

        orderService.createOrder(orderDTO);

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaLoggingService, times(1)).log(anyString(), anyString());
    }
}

