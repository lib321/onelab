package com.onelab.microservices.controller;

import com.onelab.microservices.dto.InventoryItemDTO;
import com.onelab.microservices.dto.InventoryRequestDTO;
import com.onelab.microservices.dto.InventoryResponseDTO;
import com.onelab.microservices.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/add")
    public ResponseEntity<InventoryItemDTO> addProduct(@RequestBody InventoryItemDTO itemDTO) {
        return ResponseEntity.ok(inventoryService.add(itemDTO));
    }

    @GetMapping("/stock/{productId}")
    public ResponseEntity<Integer> getStock(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getStockByProductId(productId));
    }

    @PostMapping("/reserve")
    public ResponseEntity<InventoryResponseDTO> reserveProduct(@RequestBody InventoryRequestDTO requestDTO) {
        return ResponseEntity.ok(inventoryService.checkAndReserveInventory(requestDTO));
    }

    @PutMapping("/restock")
    public ResponseEntity<InventoryItemDTO> restockProduct(@RequestBody InventoryItemDTO itemDTO) {
        return ResponseEntity.ok(inventoryService.restockProduct(itemDTO));
    }

    @GetMapping("/check-stock/{productId}")
    public ResponseEntity<Boolean> checkStock(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.isStockLow(productId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<InventoryItemDTO>> getAllProducts() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        inventoryService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all-stock")
    public ResponseEntity<Map<Long, Integer>> getAllStock() {
        Map<Long, Integer> stockMap = inventoryService.getAllStock();
        return ResponseEntity.ok(stockMap);
    }

    @PostMapping("/check-stock-order")
    public ResponseEntity<Map<Long, Boolean>> checkStockAvailability(@RequestBody List<InventoryItemDTO> items) {
        Map<Long, Boolean> stockStatus = inventoryService.checkStock(items);
        return ResponseEntity.ok(stockStatus);
    }
}
