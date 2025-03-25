package com.onelab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryUpdateDTO {
    private Long productId;
    private int oldQuantity;
    private int newQuantity;
}
