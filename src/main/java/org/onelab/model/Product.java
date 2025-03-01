package org.onelab.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class Product {

    private int id;
    private String name;
    private int price;
    private List<OrderProducts> orderProducts;
}
