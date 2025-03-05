package org.onelab.serviceImpl;

import org.onelab.model.Orders;
import org.onelab.model.Users;
import org.onelab.repository.OrderRepo;
import org.onelab.repository.UserRepo;
import org.onelab.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private OrderRepo orderRepo;

    public OrderServiceImpl(OrderRepo orderRepo) {
        this.orderRepo = orderRepo;
    }

    @Override
    public List<Orders> getOrders() {
        return orderRepo.findAll();
    }

    @Override
    public Optional<Orders> getOrderById(int id) {
        return orderRepo.findById(id);
    }

    @Override
    public Orders save(Orders order) {
        return orderRepo.save(order);
    }

    @Override
    public void deleteById(int id) {
        orderRepo.deleteById(id);
    }

    @Override
    public List<Orders> findOrdersByUserId(int userId) {
        return orderRepo.findByUserId(userId);
    }
}
