package com.onelab.microservices.repository;

import com.onelab.microservices.model.InventoryItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface InventoryRepository extends ElasticsearchRepository<InventoryItem, String> {

    Optional<InventoryItem> findByProductName(String productName);
    Optional<InventoryItem> findByProductId(Long productId);
}
