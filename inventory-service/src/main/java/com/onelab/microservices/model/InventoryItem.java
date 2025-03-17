package com.onelab.microservices.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(indexName = "inventory")
public class InventoryItem {

    @Id
    private String id;
    private Long productId;
    private String productName;
    private int quantity;
}
