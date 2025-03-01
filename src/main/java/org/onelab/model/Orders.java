package org.onelab.model;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@ToString
public class Orders {

    private int id;
    private int userId;
    private List<OrderProducts> orderProducts;
}
