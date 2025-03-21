package com.onelab.microservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductByCategoryDTO {

    private Long productId;
    private String productName;
    private int price;
    private int quantity;
    private String categoryName;
}
