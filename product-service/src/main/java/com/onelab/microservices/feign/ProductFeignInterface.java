package com.onelab.microservices.feign;

import com.onelab.dto.InventoryItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient("INVENTORY-SERVICE")
public interface ProductFeignInterface {

    @PostMapping("/api/inventory/add")
    ResponseEntity<InventoryItemDTO> addProduct(@RequestBody InventoryItemDTO itemDTO);

    @DeleteMapping("/api/inventory/delete/{productId}")
    ResponseEntity<Void> deleteProduct(@PathVariable Long productId);

    @GetMapping("/api/inventory/stock/{productId}")
    ResponseEntity<Integer> getStock(@PathVariable Long productId);

    @PutMapping("/api/inventory/restock")
    ResponseEntity<InventoryItemDTO> restockProduct(@RequestBody InventoryItemDTO itemDTO);

    @GetMapping("/api/inventory/all")
    ResponseEntity<List<InventoryItemDTO>> getAllProducts();

    @GetMapping("/api/inventory/all-stock")
    ResponseEntity<Map<Long, Integer>> getAllStock();
}
