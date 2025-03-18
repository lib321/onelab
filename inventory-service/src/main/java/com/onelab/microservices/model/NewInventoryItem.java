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

@Document(indexName = "new_inventory")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewInventoryItem {
    @Id
    private String id;
    private Long productId;
    private String productName;
    private int quantity;
    private String categoryName;
    private int price;

    @Field(type= FieldType.Date, format={}, pattern="uuuu.MM.dd")
    private LocalDate addedAt;
}

