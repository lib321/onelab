package org.onelab.dto;

import lombok.Data;

@Data
public class OrderTotalDTO {

    private int orderId;
    private long totalCount;

    public OrderTotalDTO(int orderId, long totalCount) {
        this.orderId = orderId;
        this.totalCount = totalCount;
    }
}

