package org.onelab.repository;

import org.onelab.dto.Order;
import org.onelab.dto.User;

import java.util.List;
import java.util.Optional;

public interface UserRepo {

    void save(User user);

    List<User> findAll();

    Optional<User> findById(int id);

    void remove(User user);

    Order findOrderByUserIdAndOrderId(int userId, int orderId);
}
