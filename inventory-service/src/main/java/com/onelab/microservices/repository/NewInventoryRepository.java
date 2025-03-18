package com.onelab.microservices.repository;

import com.onelab.microservices.model.NewInventoryItem;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface NewInventoryRepository extends ElasticsearchRepository<NewInventoryItem, String> {

    @Query("{\"match\": {\"categoryName\": {\"query\": \"?0\"}}}")
    List<NewInventoryItem> findByCategoryName(String categoryName);

    @Query("""
                {
                  "bool": {
                    "must": [
                      {
                        "range": {
                          "price": {
                            "gte": ?0,
                            "lte": ?1
                          }
                        }
                      }
                    ]
                  }
                }
            """)
    List<NewInventoryItem> findByPriceBetween(int minPrice, int maxPrice);

}
