package com.onelab.microservices.service;

import com.onelab.microservices.dto.*;
import com.onelab.dto.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        validateOrderRequest(request);

        request.getItems().forEach(item -> validateAndReserveProduct(item, request.getCustomerName()));

        Order order = saveOrder(request);
        return toOrderResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(Long id, String authHeader) {
        validateUser(authHeader);
        return orderRepository.findById(id)
                .map(this::toOrderResponseDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));
    }

    @Transactional
    public void deleteOrder(Long id, String authHeader) {
        validateUser(authHeader);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));

        kafkaProducerService.sendMessage("order-events-update", "DELETE", toInventoryUpdateList(order));
        orderRepository.delete(order);
        kafkaProducerService.sendMessage("order-events", "DELETE", "Order ID: " + id);
    }


    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByCustomerName(String customerName, String authHeader) {
        checkAdminAccess(authHeader);
        return orderRepository.findByCustomerName(customerName).stream()
                .map(this::toOrderResponseDTO)
                .toList();
    }

    @Transactional
    public OrderResponseDTO updateOrder(Long id, OrderUpdateDTO updateRequest, String authHeader) {
        validateUser(authHeader);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден"));

        Map<Long, Boolean> stockAvailability = orderFeignInterface.checkStockAvailability(updateRequest.getUpdatedItems());
        List<InventoryUpdateDTO> inventoryUpdates = new ArrayList<>();

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

            inventoryUpdates.add(new InventoryUpdateDTO(updateItem.getProductId(), oldQuantity, newQuantity));
        });

        if (order.getItems().isEmpty()) {
            orderRepository.delete(order);
            kafkaProducerService.sendMessage("order-events", "DELETE", "Order ID: " + id);
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Заказ удален, так как в нем не осталось товаров");
        }

        orderRepository.save(order);
        kafkaProducerService.sendMessage("order-events-update", "UPDATE", inventoryUpdates);

        return toOrderResponseDTO(order);
    }

    private OrderResponseDTO toOrderResponseDTO(Order order) {
        List<OrderItemDTO> items = order.getItems().stream()
                .map(item -> new OrderItemDTO(item.getProductId(), item.getProductName(), item.getQuantity()))
                .toList();

        return new OrderResponseDTO(order.getId(), order.getCustomerName(), items);
    }

    private Order saveOrder(OrderRequestDTO request) {
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());

        List<OrderItem> items = request.getItems().stream()
                .map(itemDTO -> OrderItem.builder()
                        .productId(itemDTO.getProductId())
                        .productName(itemDTO.getProductName())
                        .quantity(itemDTO.getQuantity())
                        .order(order)
                        .build())
                .toList();

        order.setItems(items);
        return orderRepository.save(order);
    }

    private void validateAndReserveProduct(OrderItemDTO item, String customerName) {
        validateOrderItem(item);
        InventoryRequestDTO inventoryRequest = new InventoryRequestDTO(item.getProductName(), item.getQuantity(), customerName);
        kafkaProducerService.sendMessage("order-events", "CREATE", inventoryRequest);

        ResponseEntity<InventoryResponseDTO> response = orderFeignInterface.reserveProduct(inventoryRequest);
        if (!Objects.requireNonNull(response.getBody()).isAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно товара: " + item.getProductName());
        }
    }

    private List<InventoryUpdateDTO> toInventoryUpdateList(Order order) {
        return order.getItems().stream()
                .map(item -> new InventoryUpdateDTO(item.getProductId(), item.getQuantity(), 0))
                .toList();
    }

    private void validateOrderRequest(OrderRequestDTO request) {
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Имя пользователя не может быть пустым");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Список товаров не может быть пустым");
        }

        request.getItems().forEach(this::validateOrderItem);
    }

    private void validateOrderItem(OrderItemDTO item) {
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
            if (!Boolean.TRUE.equals(userFeignInterface.validateUserRole(authHeader, "ADMIN"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
            }
        } catch (FeignException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизованный пользователь");
        }
    }
}

