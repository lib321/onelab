package org.onelab.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class OrderProducts {

    private int orderId;
    private int productId;
    private Product product;
}
