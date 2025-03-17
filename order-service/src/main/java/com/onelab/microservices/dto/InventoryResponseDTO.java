package com.onelab.microservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryResponseDTO {
    private Long productId;
    private String productName;
    private int quantity;
    private String customerName;
    private boolean available;
}
