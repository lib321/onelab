package com.onelab.microservices.service;

import com.onelab.microservices.dto.OrderDTO;
import com.onelab.microservices.model.Order;
import com.onelab.microservices.model.OrderItem;
import com.onelab.microservices.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaLoggingService kafkaLoggingService;

    public void createOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setCustomerName(orderDTO.getCustomerName());

        OrderItem orderItem = new OrderItem();
        orderItem.setProductName(orderDTO.getProductName());
        orderItem.setQuantity(orderDTO.getQuantity());
        orderItem.setOrder(order);

        order.setItems(List.of(orderItem));
        orderRepository.save(order);

        kafkaLoggingService.log("INFO", "Заказ успешно создан" + orderDTO);
    }
}

