package org.onelab.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class Users {

    private int id;
    private String firstname;
    private String lastname;
    private List<Orders> orders;
}
