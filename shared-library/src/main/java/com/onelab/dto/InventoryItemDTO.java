package com.onelab.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryItemDTO {

    private Long productId;
    private String productName;
    private int price;
    private int quantity;
    private String categoryName;

    private LocalDate addedAt;

    private LocalDate updatedAt;
}
