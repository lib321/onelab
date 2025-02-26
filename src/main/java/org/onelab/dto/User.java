package org.onelab.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Builder
@Getter
@Setter
public class User {

    private int id;
    private String firstname;
    private String lastname;
    private Set<Order> orders;
}
