package com.onelab.microservices.service;

import com.onelab.microservices.dto.*;
import com.onelab.dto.*;
import com.onelab.microservices.event.KafkaProducerService;
import com.onelab.microservices.model.InventoryItem;
import com.onelab.microservices.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaProducerService kafkaProducerService;

    public InventoryItemDTO add(InventoryItemDTO itemDTO) {
        if (inventoryRepository.existsByProductId(itemDTO.getProductId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Продукт с таким id уже существует");
        }
        InventoryItem item = InventoryItem.builder()
                .productId(itemDTO.getProductId())
                .productName(itemDTO.getProductName())
                .price(itemDTO.getPrice())
                .quantity(itemDTO.getQuantity())
                .categoryName(itemDTO.getCategoryName())
                .addedAt(itemDTO.getAddedAt())
                .updatedAt(itemDTO.getUpdatedAt()).build();

        inventoryRepository.save(item);
        return convertToDTO(item);
    }

    @Transactional
    public InventoryResponseDTO checkAndReserveInventory(InventoryRequestDTO request) {
        InventoryItem item = inventoryRepository.findByProductName(request.getProductName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Продукт не найден"));

        if (item.getQuantity() < request.getQuantity()) {
            kafkaProducerService.sendMessage("order-events", "NOT_ENOUGH", request);
            return new InventoryResponseDTO(item.getProductId(), request.getProductName(), request.getQuantity(), request.getCustomerName(), false);
        }

        item.setQuantity(item.getQuantity() - request.getQuantity());
        item.setUpdatedAt(LocalDate.now());
        inventoryRepository.save(item);

        kafkaProducerService.sendMessage("order-events", "CONFIRMED", request);
        kafkaProducerService.sendMessage("product-quantity-update", "UPDATE", new UpdateQuantityDTO(item.getProductId(), item.getQuantity()));

        return new InventoryResponseDTO(item.getProductId(), item.getProductName(), request.getQuantity(), request.getCustomerName(), true);
    }

    public int getStockByProductId(Long productId) {
        return getInventoryItemByProductId(productId).getQuantity();
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> getAllStock() {
        List<InventoryItem> items = new ArrayList<>();
        inventoryRepository.findAll().forEach(items::add);

        return items.stream()
                .collect(Collectors.toMap(InventoryItem::getProductId, InventoryItem::getQuantity));
    }

    @Transactional
    public InventoryItemDTO restockProduct(InventoryItemDTO inventoryItemDTO) {
        InventoryItem item = getInventoryItemByProductId(inventoryItemDTO.getProductId());

        item.setQuantity(inventoryItemDTO.getQuantity());
        item.setProductName(inventoryItemDTO.getProductName());
        item.setUpdatedAt(LocalDate.now());

        inventoryRepository.save(item);
        return convertToDTO(item);
    }

    public boolean isStockLow(Long productId) {
        return getStockByProductId(productId) < 2;
    }

    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getAllItems() {
        List<InventoryItemDTO> items = new ArrayList<>();
        inventoryRepository.findAll().forEach(item ->
                items.add(convertToDTO(item)
        ));
        return items;
    }

    @Transactional
    public void deleteProduct(Long productId) {
        if (!inventoryRepository.existsByProductId(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Продукт не найден");
        }
        inventoryRepository.deleteByProductId(productId);
    }

    @Transactional(readOnly = true)
    public Map<Long, Boolean> checkStock(List<InventoryItemDTO> items) {
        Map<Long, Boolean> stockMap = new HashMap<>();
        for (InventoryItemDTO item : items) {
            Optional<InventoryItem> inventoryItem = inventoryRepository.findByProductId(item.getProductId());
            int stockQuantity = inventoryItem.map(InventoryItem::getQuantity).orElse(0);
            stockMap.put(item.getProductId(), stockQuantity >= item.getQuantity());
        }
        return stockMap;
    }

    @Transactional
    public void updateInventory(InventoryUpdateDTO updateDTO) {
        InventoryItem item = getInventoryItemByProductId(updateDTO.getProductId());

        item.setQuantity(item.getQuantity() - (updateDTO.getNewQuantity() - updateDTO.getOldQuantity()));
        item.setUpdatedAt(LocalDate.now());

        inventoryRepository.save(item);
        kafkaProducerService.sendMessage("product-quantity-update", "UPDATE", new UpdateQuantityDTO(item.getProductId(), item.getQuantity()));
    }

    public List<ItemDTO> getItemsByCategoryAndPriceRange(String category, int min, int max) {
        return inventoryRepository.findByCategoryAndPriceRange(category, min, max).stream()
                .map(item -> new ItemDTO(item.getProductName(), item.getPrice(), item.getQuantity()))
                .sorted(Comparator.comparingInt(ItemDTO::price))
                .collect(Collectors.toList());
    }


    public List<ItemDTO> searchByProductName(String keyword) {
        return inventoryRepository.findByProductNameMatch(keyword).stream()
                .map(item -> new ItemDTO(item.getProductName(), item.getPrice(), item.getQuantity()))
                .collect(Collectors.toList());
    }

    private InventoryItem getInventoryItemByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Продукт не найден"));
    }

    private InventoryItemDTO convertToDTO(InventoryItem item) {
        return new InventoryItemDTO(item.getProductId(), item.getProductName(), item.getPrice(),
                item.getQuantity(), item.getCategoryName(), item.getAddedAt(), item.getUpdatedAt());
    }

}

