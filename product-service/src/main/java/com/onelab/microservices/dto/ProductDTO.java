package com.onelab.microservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {

    private Long productId;
    private String productName;
    private int price;
    private int quantity;
    private long categoryId;
    private LocalDate localDate;
}
