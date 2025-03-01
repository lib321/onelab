package org.onelab.model;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
public class Orders implements Serializable{
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private List<OrderProducts> orderProducts;
}
