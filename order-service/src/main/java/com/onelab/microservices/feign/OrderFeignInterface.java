package com.onelab.microservices.feign;

import com.onelab.microservices.dto.InventoryRequestDTO;
import com.onelab.microservices.dto.InventoryResponseDTO;
import com.onelab.microservices.dto.OrderItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;


@FeignClient("INVENTORY-SERVICE")
public interface OrderFeignInterface {

    @PostMapping("/api/inventory/reserve")
    ResponseEntity<InventoryResponseDTO> reserveProduct(@RequestBody InventoryRequestDTO requestDTO);

    @PostMapping("/api/inventory/check-stock-order")
    Map<Long, Boolean> checkStockAvailability(@RequestBody List<OrderItemDTO> items);
}
