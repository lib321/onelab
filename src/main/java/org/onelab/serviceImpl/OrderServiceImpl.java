package org.onelab.serviceImpl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.onelab.dto.OrderTotalDTO;
import org.onelab.model.Orders;
import org.onelab.repository.OrderProductRepo;
import org.onelab.repository.OrderRepo;
import org.onelab.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private OrderRepo orderRepo;
    private OrderProductRepo orderProductRepo;

    public OrderServiceImpl(OrderRepo orderRepo, OrderProductRepo orderProductRepo) {
        this.orderRepo = orderRepo;
        this.orderProductRepo = orderProductRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Orders> getOrders() {
        return orderRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public List<Orders> findOrdersByUserId(int userId) {
        return orderRepo.findByUserId(userId);
    }

    @Override
    public List<OrderTotalDTO> getTotalProductsPerOrderByUserId(int userId) {
        return orderProductRepo.getTotalProductsPerOrderByUserId(userId);
    }
}
