package org.onelab.service;

import org.onelab.dto.Order;
import org.onelab.dto.Product;
import org.onelab.dto.User;
import org.onelab.repoimpl.OrderRepoImpl;
import org.onelab.repoimpl.ProductRepoImpl;
import org.onelab.repoimpl.UserRepoImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    public void addProduct(Product product) {
        productRepo.save(product);
    }

    public List<Product> getProducts() {
        return productRepo.findAll();
    }

    public Optional<Product> getProductById(int productId) {
        return productRepo.findById(productId);
    }

    public void deleteProduct(Product product) {
        productRepo.remove(product);
    }

    public void addOrder(Order order) {
        orderRepo.save(order);
    }

    public List<Order> getOrders() {
        return orderRepo.findAll();
    }

    public Optional<Order> getOrderById(int orderId) {
        return orderRepo.findById(orderId);
    }

    public void deleteOrder(Order order) {
        orderRepo.remove(order);
    }

    public void addProductToOrder(Product product, Order order) {
        List<Product> products = order.getProducts();
        products.add(product);
        order.setProducts(products);
        product.setOrder(order);
    }

    public void addOrderToUser(Order order, User user) {
        Set<Order> orders = user.getOrders();
        orders.add(order);
        user.setOrders(orders);
        order.setUser(user);
        userRepo.save(user);
    }

    public Order getOrderFromUser(int userId, int orderId) {
        return userRepo.findOrderByUserIdAndOrderId(userId, orderId);
    }

    public void deleteProductFromOrder(Order order, User user, Product product) {
        Order order1 = getOrderFromUser(order.getId(), user.getId());
        order1.getProducts().remove(product);
        product.setOrder(null);
    }
}
