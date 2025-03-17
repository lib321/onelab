package com.onelab.microservices.controller;

import com.onelab.microservices.dto.OrderRequestDTO;
import com.onelab.microservices.dto.OrderResponseDTO;
import com.onelab.microservices.dto.OrderUpdateDTO;
import com.onelab.microservices.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/add")
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO orderRequest,
                                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(orderService.createOrder(orderRequest, authHeader));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(orderService.getOrder(id, authHeader));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        orderService.deleteOrder(id, authHeader);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all/{customerName}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByCustomerName(@PathVariable String customerName,
                                                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerName(customerName, authHeader));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrder(@PathVariable Long id, @RequestBody OrderUpdateDTO updateRequest,
                                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(orderService.updateOrder(id, updateRequest, authHeader));
    }
}