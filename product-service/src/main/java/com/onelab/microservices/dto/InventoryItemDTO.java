package com.onelab.microservices.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
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

    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate addedAt;

    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate updatedAt;
}
