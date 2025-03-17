package com.onelab.microservices.event;

import com.onelab.microservices.dto.OrderDTO;
import com.onelab.microservices.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "order-topic", groupId = "order-group")
    public void consume(OrderDTO orderDTO) {
        orderService.createOrder(orderDTO);
    }
}

