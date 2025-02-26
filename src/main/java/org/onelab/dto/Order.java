package org.onelab.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class Order {

    private int id;
    private String name;
    private List<Product> products;
    private User user;
}
