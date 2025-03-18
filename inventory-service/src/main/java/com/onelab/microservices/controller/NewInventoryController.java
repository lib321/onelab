package com.onelab.microservices.controller;


import com.onelab.microservices.model.Category;
import com.onelab.microservices.model.NewInventoryItem;
import com.onelab.microservices.service.NewInventoryService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/new-inventory")
@RequiredArgsConstructor
public class NewInventoryController {

    private final NewInventoryService inventoryService;

    @GetMapping("/products/{category}")
    public ResponseEntity<List<String>> getProductsByCategory(@PathVariable String category) {
        List<String> productNames = inventoryService.getProductNamesByCategory(category);
        return ResponseEntity.ok(productNames);
    }

    @GetMapping("/products/by-price")
    public ResponseEntity<List<String>> getProductsByPrice(
            @RequestParam int minPrice,
            @RequestParam int maxPrice) {

        List<String> productNames = inventoryService.getProductNamesByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(productNames);
    }

    @PostMapping("/add/category")
    public ResponseEntity<Category> addCategory(@RequestBody Category category) {
        return inventoryService.addCategory(Optional.ofNullable(category))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/add/item")
    public ResponseEntity<NewInventoryItem> addItem(@RequestBody NewInventoryItem item) {
        return inventoryService.addItem(Optional.ofNullable(item))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<NewInventoryItem>> filterItemsByPrice(
            @RequestParam int minPrice) {
        List<NewInventoryItem> filteredItems = inventoryService.filterWithLambda(
                item -> item.getPrice() >= minPrice);
        return ResponseEntity.ok(filteredItems);
    }

    @GetMapping("/compare")
    public ResponseEntity<String> compareStreamPerformance() {
        inventoryService.compareStreamPerformance();
        return ResponseEntity.ok("Сравнение производительности sequential and parallel streams");
    }

    @GetMapping("/total-price")
    public ResponseEntity<Integer> getTotalInventoryPrice() {
        return ResponseEntity.ok(inventoryService.getTotalInventoryValue());
    }

    @GetMapping("/group-by-category")
    public ResponseEntity<Map<String, List<NewInventoryItem>>> groupByCategory() {
        return ResponseEntity.ok(inventoryService.groupByCategory());
    }

    @GetMapping("/partition-by-price")
    public ResponseEntity<Map<Boolean, List<NewInventoryItem>>> partitionByPrice(@RequestParam int price) {
        return ResponseEntity.ok(inventoryService.partitionByPrice(price));
    }
}

