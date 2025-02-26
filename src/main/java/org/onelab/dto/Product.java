package org.onelab.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Product {

    private int id;
    private String name;
    private int price;
    private Order order;
}
