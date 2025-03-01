package org.onelab.service;

import org.onelab.model.OrderProducts;
import org.onelab.model.Orders;
import org.onelab.model.Product;
import org.onelab.model.Users;
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

    public List<Users> findAllUsers() {
        return userRepo.findAll();
    }

    public Optional<Users> findUserById(int id) {
        return userRepo.findById(id);
    }

    public void saveUser(Users user) {
        userRepo.save(user);
    }

    public void updateUser(Users user, int id) {
        userRepo.update(user, id);
    }

    public void removeUser(int id) {
        userRepo.remove(id);
    }

    public List<Orders> findAllOrders() {
        return orderRepo.findAll();
    }

    public List<Orders> findAllByUserId(int id) {
        return orderRepo.findAllByUserId(id);
    }

    public Optional<Orders> findOrderByUserId(int userId) {
        return orderRepo.findById(userId);
    }

    public void saveOrder(Orders order) {
        orderRepo.save(order);
    }

    public void removeOrder(int id) {
        orderRepo.remove(id);
    }

    public List<OrderProducts> findProductsByOrderId(int orderId) {
        return orderRepo.findProductsByOrderId(orderId);
    }
}
