package org.onelab.repoimpl;

import org.onelab.dto.Order;
import org.onelab.dto.User;
import org.onelab.repository.UserRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepoImpl implements UserRepo {

    private List<User> users = new ArrayList<>();

    @Override
    public void save(User user) {
        users.add(user);
    }

    @Override
    public List<User> findAll() {
        return users;
    }

    @Override
    public Optional<User> findById(int userId) {
        if (users.isEmpty()) return Optional.empty();

        for (User user : users) {
            if (user.getId() == userId) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    @Override
    public void remove(User user) {
        users.remove(user);
    }

    @Override
    public Order findOrderByUserIdAndOrderId(int userId, int orderId) {
        Optional<User> user = findById(userId);
        Order order = null;
        for (Order order1 : user.get().getOrders()) {
            if (order1.getId() == orderId) {
                order = order1;
            }
        }
        return order;
    }

}
