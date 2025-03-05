package org.onelab.service;

import org.onelab.model.Users;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<Users> getUsers();

    Optional<Users> getUserById(int id);

    Users save(Users user);

    void deleteById(int id);
}
