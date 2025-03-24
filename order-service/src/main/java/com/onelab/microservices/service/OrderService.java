package com.onelab.microservices.service;

import com.onelab.microservices.dto.*;
import com.onelab.microservices.event.KafkaProducerService;
import com.onelab.microservices.feign.OrderFeignInterface;
import com.onelab.microservices.feign.UserFeignInterface;
import com.onelab.microservices.model.Order;
import com.onelab.microservices.model.OrderItem;
import com.onelab.microservices.repository.OrderRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderFeignInterface orderFeignInterface;
    private final UserFeignInterface userFeignInterface;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request, String authHeader) {
        validateUser(authHeader);

        try {
            validateOrder(request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        for (OrderItemDTO item : request.getItems()) {
            InventoryRequestDTO inventoryRequest = new InventoryRequestDTO(item.getProductName(), item.getQuantity(), request.getCustomerName());
            kafkaProducerService.sendMessage("order-events", "CREATE", inventoryRequest);

            ResponseEntity<InventoryResponseDTO> response = orderFeignInterface.reserveProduct(inventoryRequest);

            if (!response.getBody().isAvailable()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно товара: " + item.getProductName());
            }
        }

        Order order = new Order();
        order.setCustomerName(request.getCustomerName());

        List<OrderItem> items = request.getItems().stream().map(itemDTO -> {
            OrderItem item = new OrderItem();
            item.setProductId(itemDTO.getProductId());
            item.setProductName(itemDTO.getProductName());
            item.setQuantity(itemDTO.getQuantity());
            item.setOrder(order);
            return item;
        }).collect(Collectors.toList());

        order.setItems(items);
        orderRepository.save(order);
        return new OrderResponseDTO(order.getId(), order.getCustomerName(), request.getItems());
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(Long id, String authHeader) {
        validateUser(authHeader);

        return orderRepository.findById(id)
                .map(order -> new OrderResponseDTO(
                        order.getId(),
                        order.getCustomerName(),
                        order.getItems().stream()
                                .map(item -> new OrderItemDTO(item.getProductId(), item.getProductName(), item.getQuantity()))
                                .collect(Collectors.toList())
                ))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));
    }

    @Transactional
    public void deleteOrder(Long id, String authHeader) {
        validateUser(authHeader);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));

        List<InventoryUpdateDTO> updates = order.getItems().stream()
                .map(item -> new InventoryUpdateDTO(item.getProductId(), item.getQuantity(), 0))
                .collect(Collectors.toList());

        kafkaProducerService.sendMessage("order-events-update", "DELETE", updates);

        orderRepository.delete(order);
        kafkaProducerService.sendMessage("order-events", "DELETE", "Order ID: " + id);
    }


    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByCustomerName(String customerName, String authHeader) {
        checkAdminAccess(authHeader);

        List<Order> orders = orderRepository.findByCustomerName(customerName);

        return orders.stream()
                .map(order -> new OrderResponseDTO(
                        order.getId(),
                        order.getCustomerName(),
                        order.getItems().stream()
                                .map(item -> new OrderItemDTO(item.getProductId(), item.getProductName(), item.getQuantity()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDTO updateOrder(Long id, OrderUpdateDTO updateRequest, String authHeader) {
        validateUser(authHeader);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));

        Map<Long, Boolean> stockAvailability = orderFeignInterface.checkStockAvailability(updateRequest.getUpdatedItems());

        updateRequest.getUpdatedItems().forEach(updateItem -> {
            if (!stockAvailability.getOrDefault(updateItem.getProductId(), false)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно товара на складе");
            }

            OrderItem orderItem = order.getItems().stream()
                    .filter(item -> item.getProductId().equals(updateItem.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Товар не найден в заказе"));

            int oldQuantity = orderItem.getQuantity();
            int newQuantity = updateItem.getQuantity();

            if (newQuantity == 0) {
                order.getItems().remove(orderItem);
            } else {
                orderItem.setQuantity(newQuantity);
            }

            kafkaProducerService.sendMessage("order-events-update", "UPDATE",
                    new InventoryUpdateDTO(updateItem.getProductId(), oldQuantity, newQuantity));
        });

        if (order.getItems().isEmpty()) {
            orderRepository.delete(order);
            kafkaProducerService.sendMessage("order-events", "DELETE", "Order ID: " + id);
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Заказ удален, так как в нем не осталось товаров");
        }

        orderRepository.save(order);

        return new OrderResponseDTO(order.getId(), order.getCustomerName(),
                order.getItems().stream()
                        .map(item -> new OrderItemDTO(item.getProductId(), item.getProductName(), item.getQuantity()))
                        .collect(Collectors.toList()));
    }

    private void validateOrder(OrderRequestDTO request) {
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Список товаров не может быть пустым");
        }

        for (OrderItemDTO item : request.getItems()) {
            if (item.getProductId() == null || item.getProductId() <= 0) {
                throw new IllegalArgumentException("Некорректный ID товара: " + item.getProductId());
            }
            if (item.getProductName() == null || item.getProductName().isBlank()) {
                throw new IllegalArgumentException("Название товара не может быть пустым");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Количество товара должно быть больше 0");
            }
        }
    }

    private void validateUser(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизованный пользователь");
        }

        try {
            userFeignInterface.validateUser(authHeader);
        } catch (FeignException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизованный пользователь");
        }
    }

    private void checkAdminAccess(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизованный пользователь");
        }

        try {
            Boolean isAdmin = userFeignInterface.validateUserRole(authHeader, "ADMIN");
            if (Boolean.FALSE.equals(isAdmin)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
            }
        } catch (FeignException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизованный пользователь");
        }
    }
}

