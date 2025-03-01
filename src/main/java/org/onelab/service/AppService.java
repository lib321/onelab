package org.onelab.service;

import org.onelab.model.Orders;
import org.onelab.model.Product;
import org.onelab.repoimpl.OrderRepoImpl;
import org.onelab.repoimpl.ProductRepoImpl;
import org.onelab.repoimpl.UserRepoImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class AppService {

    private OrderRepoImpl orderRepo;
    private ProductRepoImpl productRepo;
    private UserRepoImpl userRepo;

    @Autowired
    public AppService(OrderRepoImpl orderRepo, ProductRepoImpl productRepo, UserRepoImpl userRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
    }
}
