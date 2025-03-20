package com.onelab.microservices.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

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
    private int price;
    private int quantity;
    private String categoryName;

    @Field(type= FieldType.Date, format={}, pattern="uuuu.MM.dd")
    private LocalDate addedAt;

    @Field(type= FieldType.Date, format={}, pattern="uuuu.MM.dd")
    private LocalDate updatedAt;
}
