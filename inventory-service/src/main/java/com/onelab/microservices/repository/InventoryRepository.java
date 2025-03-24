package com.onelab.microservices.repository;

import com.onelab.microservices.model.InventoryItem;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.Optional;


public interface InventoryRepository extends ElasticsearchRepository<InventoryItem, String> {

    Optional<InventoryItem> findByProductName(String productName);
    Optional<InventoryItem> findByProductId(Long productId);
    boolean existsByProductId(Long productId);
    void deleteByProductId(Long productId);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"categoryName\": \"?0\"}}], \"filter\": [{\"range\": {\"price\": {\"gte\": ?1, \"lte\": ?2}}}]}}")
    List<InventoryItem> findByCategoryAndPriceRange(String category, int minPrice, int maxPrice);

    @Query("{\"bool\": {\"must\": [{\"match_phrase\": {\"productName\": \"?0\"}}]}}")
    List<InventoryItem> findByProductNameMatch(String keyword);
}
